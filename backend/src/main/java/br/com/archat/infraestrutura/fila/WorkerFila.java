package br.com.archat.infraestrutura.fila;

import br.com.archat.aplicacao.mensagem.ConfirmarRecebimentoUseCase;
import br.com.archat.aplicacao.mensagem.ProcessarMensagemUseCase;
import br.com.archat.aplicacao.mensagem.RecuperarFilaUseCase;
import br.com.archat.dominio.mensagem.StatusMensagem;
import br.com.archat.infraestrutura.config.ArChatPropriedades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Consome a fila em threads próprias, independentes das requisições HTTP. Ao iniciar, reconstrói a fila
 * a partir do banco; ao parar, termina as mensagens em andamento.
 */
@Component
class WorkerFila implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(WorkerFila.class);
    private static final Duration ESPERA_FILA_VAZIA = Duration.ofSeconds(1);

    private final FilaMensagensEmMemoria fila;
    private final ProcessarMensagemUseCase processarMensagem;
    private final ConfirmarRecebimentoUseCase confirmarRecebimento;
    private final RecuperarFilaUseCase recuperarFila;
    private final ArChatPropriedades.Fila configuracao;
    /** Só o canal simulado confirma a entrega sozinho; com a Z-API a entrega real chega pelo WhatsApp. */
    private final boolean simularEntrega;

    private volatile boolean executando;
    private ExecutorService workers;
    private ScheduledExecutorService confirmacoes;

    WorkerFila(FilaMensagensEmMemoria fila, ProcessarMensagemUseCase processarMensagem,
               ConfirmarRecebimentoUseCase confirmarRecebimento, RecuperarFilaUseCase recuperarFila,
               ArChatPropriedades propriedades,
               @Value("${archat.envio.provedor:simulado}") String provedorDeEnvio) {
        this.fila = fila;
        this.processarMensagem = processarMensagem;
        this.confirmarRecebimento = confirmarRecebimento;
        this.recuperarFila = recuperarFila;
        this.configuracao = propriedades.fila();
        this.simularEntrega = "simulado".equals(provedorDeEnvio);
    }

    @Override
    public void start() {
        int recuperadas = recuperarFila.executar();
        log.info("Fila reconstruída com {} mensagem(ns) pendente(s)", recuperadas);
        executando = true;
        confirmacoes = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().name("archat-entrega").factory());
        workers = Executors.newFixedThreadPool(configuracao.workers(), Thread.ofPlatform().name("archat-worker-", 1).factory());
        for (int i = 0; i < configuracao.workers(); i++) {
            workers.submit(this::consumir);
        }
        log.info("{} worker(s) da fila iniciado(s)", configuracao.workers());
    }

    private void consumir() {
        while (executando) {
            try {
                Optional<UUID> proxima = fila.aguardarProxima(ESPERA_FILA_VAZIA);
                if (proxima.isPresent()) {
                    processar(proxima.get());
                    respeitarIntervaloEntreEnvios();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                log.error("Erro inesperado no worker da fila", e);
            }
        }
    }

    private void processar(UUID mensagemId) {
        Optional<StatusMensagem> status = processarMensagem.executar(mensagemId);
        log.debug("Mensagem {} processada: {}", mensagemId, status.orElse(null));
        if (simularEntrega && status.orElse(null) == StatusMensagem.ENVIADA) {
            confirmacoes.schedule(() -> confirmarRecebimento.confirmarEntrega(mensagemId),
                    configuracao.atrasoConfirmacaoEntrega().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void respeitarIntervaloEntreEnvios() throws InterruptedException {
        if (!configuracao.intervaloEntreEnvios().isZero()) {
            Thread.sleep(configuracao.intervaloEntreEnvios());
        }
    }

    @Override
    public void stop() {
        executando = false;
        workers.shutdown();
        confirmacoes.shutdown();
        try {
            workers.awaitTermination(10, TimeUnit.SECONDS);
            confirmacoes.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return executando;
    }
}

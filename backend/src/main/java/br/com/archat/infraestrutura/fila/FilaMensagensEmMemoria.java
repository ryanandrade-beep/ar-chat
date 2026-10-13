package br.com.archat.infraestrutura.fila;

import br.com.archat.aplicacao.porta.FilaMensagens;
import br.com.archat.dominio.fila.FilaPrioridade;
import br.com.archat.dominio.mensagem.Prioridade;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Torna a {@link FilaPrioridade} do domínio segura para várias threads: produtores (requisições HTTP)
 * enfileiram e os workers aguardam a próxima mensagem sem ficar consultando em laço.
 */
public class FilaMensagensEmMemoria implements FilaMensagens {

    private final FilaPrioridade<UUID> fila;
    private final ReentrantLock trava = new ReentrantLock();
    private final Condition naoVazia = trava.newCondition();

    public FilaMensagensEmMemoria(int urgentesSeguidas) {
        this.fila = new FilaPrioridade<>(urgentesSeguidas);
    }

    @Override
    public void enfileirar(UUID mensagemId, Prioridade prioridade) {
        trava.lock();
        try {
            fila.enfileirar(mensagemId, prioridade);
            naoVazia.signal();
        } finally {
            trava.unlock();
        }
    }

    /** Retira a próxima mensagem, esperando até {@code tempoMaximo} se a fila estiver vazia. */
    public Optional<UUID> aguardarProxima(Duration tempoMaximo) throws InterruptedException {
        trava.lock();
        try {
            long restante = tempoMaximo.toNanos();
            while (fila.vazia() && restante > 0) {
                restante = naoVazia.awaitNanos(restante);
            }
            return fila.proxima();
        } finally {
            trava.unlock();
        }
    }

    @Override
    public int tamanho(Prioridade prioridade) {
        trava.lock();
        try {
            return fila.tamanho(prioridade);
        } finally {
            trava.unlock();
        }
    }

}

package br.com.archat.infraestrutura.envio;

import br.com.archat.aplicacao.porta.EnvioMensagemGateway;
import br.com.archat.infraestrutura.config.ArChatPropriedades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simula um canal externo (WhatsApp/SMS): espera um tempo aleatório e falha com a taxa configurada.
 * Para integrar um canal real, basta outra implementação de {@link EnvioMensagemGateway}.
 */
@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "simulado", matchIfMissing = true)
class EnvioMensagemSimulado implements EnvioMensagemGateway {

    private static final Logger log = LoggerFactory.getLogger(EnvioMensagemSimulado.class);

    private final ArChatPropriedades.EnvioSimulado configuracao;

    EnvioMensagemSimulado(ArChatPropriedades propriedades) {
        this.configuracao = propriedades.envioSimulado();
    }

    @Override
    public ResultadoEnvio enviar(Envio envio) {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        long minimo = configuracao.atrasoMinimo().toMillis();
        long maximo = Math.max(minimo, configuracao.atrasoMaximo().toMillis());
        try {
            Thread.sleep(minimo == maximo ? minimo : aleatorio.nextLong(minimo, maximo + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultadoEnvio.recusado();
        }
        boolean sucesso = aleatorio.nextDouble() >= configuracao.taxaFalha();
        log.info("Envio simulado da mensagem {} ({}, tentativa {}): {}", envio.mensagemId(), envio.prioridade(),
                envio.tentativa(), sucesso ? "sucesso" : "falha");
        return sucesso ? ResultadoEnvio.aceito("SIM-" + envio.mensagemId()) : ResultadoEnvio.recusado();
    }
}

package br.com.archat.infraestrutura.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/** Configurações da aplicação, lidas do prefixo {@code archat} (application.yml / variáveis de ambiente). */
@ConfigurationProperties("archat")
public record ArChatPropriedades(Token token, AdministradorInicial administradorInicial, Fila fila,
                              EnvioSimulado envioSimulado, ZApi zapi, Webhook webhook,
                              List<String> origensPermitidas) {

    public record Token(String segredo, Duration validade) {
    }

    public record AdministradorInicial(String nome, String login, String senha) {
    }

    /**
     * @param workers               threads que consomem a fila
     * @param maximoTentativas      tentativas de envio antes da falha definitiva
     * @param urgentesSeguidas      urgentes atendidas antes de uma normal (anti-starvation)
     * @param tempoEstimadoPorMensagem usado na previsão de entrega
     * @param atrasoConfirmacaoEntrega tempo até a entrega simulada ser confirmada
     * @param intervaloEntreEnvios     pausa de cada worker após um envio; limita o ritmo dos disparos
     *                                 em massa para reduzir o risco de bloqueio do número no WhatsApp
     */
    public record Fila(int workers, int maximoTentativas, int urgentesSeguidas, Duration tempoEstimadoPorMensagem,
                       Duration atrasoConfirmacaoEntrega, Duration intervaloEntreEnvios) {
    }

    public record EnvioSimulado(Duration atrasoMinimo, Duration atrasoMaximo, double taxaFalha) {
    }

    /** Segredo que faz parte da URL dos webhooks: só quem conhece a URL consegue avisar o sistema. */
    public record Webhook(String segredo) {
    }

    /** Credenciais da instância Z-API; usadas quando {@code archat.envio.provedor=zapi}. */
    public record ZApi(String urlBase, String instancia, String token, String clientToken, Duration timeout) {
    }
}

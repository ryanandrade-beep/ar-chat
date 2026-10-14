package br.com.archat.apresentacao.dto;

import br.com.archat.aplicacao.mensagem.EnviarMensagemUseCase.MensagemEnviada;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.Remetente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class MensagemDtos {

    private MensagemDtos() {
    }

    public record SendMessageRequest(UUID conversationId, UUID recipientId,
                                     @NotBlank @Size(max = Mensagem.TAMANHO_MAXIMO) String content,
                                     @NotBlank String priority, String channel) {
    }

    public record SimulatedReplyRequest(@NotBlank @Size(max = Mensagem.TAMANHO_MAXIMO) String content) {
    }

    public record SendMessageResponse(UUID id, UUID conversationId, String status, Instant timestamp,
                                      Instant estimatedDelivery, BigDecimal cost, BigDecimal currentBalance) {

        public static SendMessageResponse de(MensagemEnviada enviada) {
            Mensagem m = enviada.mensagem();
            return new SendMessageResponse(m.id(), m.conversaId(), ApiEnums.status(m.status()), m.criadaEm(),
                    enviada.previsaoEntrega(), m.custo(), enviada.disponivel());
        }
    }

    public record SentBy(String type) {
    }

    public record MessageResponse(UUID id, UUID conversationId, UUID campaignId, String content, SentBy sentBy, Instant timestamp,
                                  String priority, String status, BigDecimal cost, int attempts, Instant updatedAt) {

        public static MessageResponse de(Mensagem m) {
            return new MessageResponse(m.id(), m.conversaId(), m.campanhaId(), m.conteudo(),
                    new SentBy(m.remetente() == Remetente.CLIENTE ? "client" : "user"), m.criadaEm(),
                    ApiEnums.prioridade(m.prioridade()), ApiEnums.status(m.status()), m.custo(), m.tentativas(),
                    m.atualizadaEm());
        }
    }

    public record MessageStatusResponse(UUID id, String status, int attempts, Instant updatedAt) {

        public static MessageStatusResponse de(Mensagem m) {
            return new MessageStatusResponse(m.id(), ApiEnums.status(m.status()), m.tentativas(), m.atualizadaEm());
        }
    }
}

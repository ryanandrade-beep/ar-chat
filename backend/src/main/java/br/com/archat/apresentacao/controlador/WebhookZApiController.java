package br.com.archat.apresentacao.controlador;

import br.com.archat.aplicacao.atendimento.ReceberMensagemUseCase;
import br.com.archat.aplicacao.excecao.NaoAutenticadoException;
import br.com.archat.aplicacao.mensagem.AtualizarStatusPeloCanalUseCase;
import br.com.archat.aplicacao.mensagem.AtualizarStatusPeloCanalUseCase.AvisoDoCanal;
import br.com.archat.infraestrutura.config.ArChatPropriedades;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Webhooks da Z-API (https://developer.z-api.io/webhooks/introduction), no formato da documentação:
 * "Ao receber" (ReceivedCallback) e "Status da mensagem" (MessageStatusCallback). A Z-API não assina as
 * chamadas, então a URL carrega um segredo; sem ele, a chamada é recusada. Configure na instância:
 * https://SEU-ENDERECO/webhooks/zapi/{segredo}/received e .../status (a Z-API exige HTTPS).
 */
@RestController
@RequestMapping("/webhooks/zapi/{segredo}")
@Tag(name = "Webhooks Z-API", description = "Chamados pela Z-API; não usam token, e sim o segredo na URL")
class WebhookZApiController {

    private static final Logger log = LoggerFactory.getLogger(WebhookZApiController.class);

    private final ReceberMensagemUseCase receber;
    private final AtualizarStatusPeloCanalUseCase atualizarStatus;
    private final byte[] segredo;

    WebhookZApiController(ReceberMensagemUseCase receber, AtualizarStatusPeloCanalUseCase atualizarStatus,
                          ArChatPropriedades propriedades) {
        this.receber = receber;
        this.atualizarStatus = atualizarStatus;
        this.segredo = propriedades.webhook().segredo().getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/received")
    @Operation(summary = "Ao receber: mensagem enviada por um contato ao número conectado")
    Map<String, Object> recebida(@PathVariable String segredo, @RequestBody RecebidaZApi corpo) {
        validar(segredo);
        if (Boolean.TRUE.equals(corpo.fromMe()) || !"ReceivedCallback".equals(corpo.type())) {
            return Map.of("ignored", true);
        }
        String texto = corpo.text() == null ? null : corpo.text().message();
        Instant quando = corpo.momment() == null ? null : Instant.ofEpochMilli(corpo.momment());
        boolean registrada = receber.doCanal(corpo.phone(), texto, corpo.messageId(), quando).isPresent();
        if (!registrada) {
            log.info("Webhook Z-API: mensagem {} de {} ignorada (repetida ou contato não cadastrado)",
                    corpo.messageId(), corpo.phone());
        }
        return Map.of("registered", registrada);
    }

    @PostMapping("/status")
    @Operation(summary = "Status da mensagem: RECEIVED (entregue) e READ/PLAYED (lida)")
    Map<String, Object> status(@PathVariable String segredo, @RequestBody StatusZApi corpo) {
        validar(segredo);
        AvisoDoCanal aviso = switch (corpo.status() == null ? "" : corpo.status()) {
            case "RECEIVED" -> AvisoDoCanal.ENTREGUE;
            case "READ", "PLAYED" -> AvisoDoCanal.LIDA;
            default -> null;
        };
        if (aviso == null || corpo.ids() == null) {
            return Map.of("updated", 0);
        }
        return Map.of("updated", atualizarStatus.aplicar(corpo.ids(), aviso));
    }

    private void validar(String informado) {
        if (!MessageDigest.isEqual(segredo, informado.getBytes(StandardCharsets.UTF_8))) {
            throw new NaoAutenticadoException("Webhook com segredo inválido");
        }
    }

    /** Campos usados do webhook "Ao receber" da Z-API. */
    record RecebidaZApi(String type, String messageId, String phone, Boolean fromMe, Boolean isGroup,
                        Long momment, String senderName, Texto text) {

        record Texto(String message) {
        }
    }

    /** Campos usados do webhook "Status da mensagem" da Z-API. */
    record StatusZApi(String type, String status, List<String> ids, String phone, Boolean isGroup) {
    }
}

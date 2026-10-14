package br.com.archat.infraestrutura.envio.zapi;

import br.com.archat.aplicacao.porta.EnvioMensagemGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Entrega a mensagem pelo WhatsApp usando a rota send-text da Z-API. */
@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "zapi")
class EnvioMensagemZApi implements EnvioMensagemGateway {

    private static final Logger log = LoggerFactory.getLogger(EnvioMensagemZApi.class);
    private static final String DDI_BRASIL = "55";

    private final ClienteZApi zapi;

    EnvioMensagemZApi(ClienteZApi zapi) {
        this.zapi = zapi;
    }

    @Override
    public ResultadoEnvio enviar(Envio envio) {
        try {
            ClienteZApi.RespostaEnvio resposta = zapi.enviarTexto(comDdi(envio.telefone()), envio.conteudo());
            boolean aceito = resposta != null && resposta.messageId() != null;
            log.info("Z-API send-text da mensagem {} (tentativa {}): {}", envio.mensagemId(), envio.tentativa(),
                    aceito ? "aceito, messageId=" + resposta.messageId() : "sem messageId");
            return aceito ? ResultadoEnvio.aceito(resposta.messageId()) : ResultadoEnvio.recusado();
        } catch (RestClientException e) {
            log.warn("Z-API recusou a mensagem {} (tentativa {}): {}", envio.mensagemId(), envio.tentativa(), e.getMessage());
            return ResultadoEnvio.recusado();
        }
    }

    static String comDdi(String telefone) {
        boolean grupo = telefone.contains("-");
        return !grupo && telefone.length() <= 11 ? DDI_BRASIL + telefone : telefone;
    }
}

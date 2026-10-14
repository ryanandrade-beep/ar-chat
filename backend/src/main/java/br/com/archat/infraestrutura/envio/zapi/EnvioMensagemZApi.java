package br.com.archat.infraestrutura.envio.zapi;

import br.com.archat.aplicacao.porta.ArquivoRepositorio;
import br.com.archat.aplicacao.porta.EnvioMensagemGateway;
import br.com.archat.dominio.arquivo.Arquivo;
import br.com.archat.dominio.mensagem.Midia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Entrega a mensagem pelo WhatsApp usando a rota da Z-API do tipo da mensagem (texto, imagem, documento, áudio ou vídeo). */
@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "zapi")
class EnvioMensagemZApi implements EnvioMensagemGateway {

    private static final Logger log = LoggerFactory.getLogger(EnvioMensagemZApi.class);
    private static final String DDI_BRASIL = "55";

    private final ClienteZApi zapi;
    private final ArquivoRepositorio arquivos;

    EnvioMensagemZApi(ClienteZApi zapi, ArquivoRepositorio arquivos) {
        this.zapi = zapi;
        this.arquivos = arquivos;
    }

    @Override
    public ResultadoEnvio enviar(Envio envio) {
        try {
            ClienteZApi.RespostaEnvio resposta = enviarPelaRota(comDdi(envio.telefone()), envio);
            boolean aceito = resposta != null && resposta.messageId() != null;
            log.info("Z-API {} da mensagem {} (tentativa {}): {}", rota(envio.midia()), envio.mensagemId(), envio.tentativa(),
                    aceito ? "aceito, messageId=" + resposta.messageId() : "sem messageId");
            return aceito ? ResultadoEnvio.aceito(resposta.messageId()) : ResultadoEnvio.recusado();
        } catch (RestClientException e) {
            log.warn("Z-API recusou a mensagem {} (tentativa {}): {}", envio.mensagemId(), envio.tentativa(), e.getMessage());
            return ResultadoEnvio.recusado();
        }
    }

    private ClienteZApi.RespostaEnvio enviarPelaRota(String telefone, Envio envio) {
        Midia midia = envio.midia();
        String legenda = envio.conteudo() == null || envio.conteudo().isBlank() ? null : envio.conteudo();
        if (midia == null) {
            return zapi.enviarTexto(telefone, envio.conteudo());
        }
        String conteudo = conteudoParaZApi(midia);
        return switch (midia.tipo()) {
            case IMAGEM -> zapi.enviarImagem(telefone, conteudo, legenda);
            case DOCUMENTO -> zapi.enviarDocumento(telefone, conteudo, midia.extensao(), midia.nomeArquivo(), legenda);
            case AUDIO -> zapi.enviarAudio(telefone, conteudo);
            case VIDEO -> zapi.enviarVideo(telefone, conteudo, legenda);
        };
    }

    private String conteudoParaZApi(Midia midia) {
        return Arquivo.idDaUrl(midia.url())
                .map(id -> arquivos.buscarPorId(id).orElseThrow(() -> new IllegalStateException("Arquivo não encontrado")))
                .map(Arquivo::comoDataUri)
                .orElse(midia.url());
    }

    static String rota(Midia midia) {
        if (midia == null) {
            return "send-text";
        }
        return switch (midia.tipo()) {
            case IMAGEM -> "send-image";
            case DOCUMENTO -> "send-document/" + midia.extensao();
            case AUDIO -> "send-audio";
            case VIDEO -> "send-video";
        };
    }

    static String comDdi(String telefone) {
        boolean grupo = telefone.contains("-");
        return !grupo && telefone.length() <= 11 ? DDI_BRASIL + telefone : telefone;
    }
}

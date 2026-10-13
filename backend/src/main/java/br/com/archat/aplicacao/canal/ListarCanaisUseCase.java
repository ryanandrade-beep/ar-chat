package br.com.archat.aplicacao.canal;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp;

import java.util.List;

/**
 * Canais de atendimento oferecidos. Hoje só o WhatsApp Web (via Z-API) está disponível; a API oficial
 * do WhatsApp Business aparece como "em breve".
 */
public class ListarCanaisUseCase {

    public static final String WHATSAPP_WEB = "whatsapp_web";
    public static final String WHATSAPP_BUSINESS = "whatsapp_business";

    private final ConexaoWhatsApp conexao;

    public ListarCanaisUseCase(ConexaoWhatsApp conexao) {
        this.conexao = conexao;
    }

    public List<Canal> listar() {
        var situacao = conexao.situacao();
        return List.of(
                new Canal(WHATSAPP_WEB, "WhatsApp Web", "Conexão por QR Code (Z-API)", true, situacao.conectado()),
                new Canal(WHATSAPP_BUSINESS, "WhatsApp Business API", "API oficial da Meta — em breve", false, false));
    }

    public record Canal(String id, String nome, String descricao, boolean disponivel, boolean conectado) {
    }
}

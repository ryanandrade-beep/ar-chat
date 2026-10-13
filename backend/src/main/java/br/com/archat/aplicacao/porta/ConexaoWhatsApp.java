package br.com.archat.aplicacao.porta;

import java.util.List;

/** Conexão do número de WhatsApp usado pelo canal de envio. */
public interface ConexaoWhatsApp {

    SituacaoConexao situacao();

    /** QR Code para conectar o número, como imagem em base64 (data URI). */
    String qrCode();

    void desconectar();

    /** Grupos de que o número participa, para descobrir o ID a cadastrar como destinatário. */
    List<GrupoWhatsApp> listarGrupos(int pagina, int tamanhoPagina);

    record GrupoWhatsApp(String id, String nome) {
    }

    record SituacaoConexao(String provedor, boolean conectado, boolean celularOnline, String detalhe) {
    }
}

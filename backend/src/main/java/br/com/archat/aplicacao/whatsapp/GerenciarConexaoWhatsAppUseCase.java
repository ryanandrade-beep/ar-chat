package br.com.archat.aplicacao.whatsapp;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp;
import br.com.archat.aplicacao.porta.ConexaoWhatsApp.SituacaoConexao;
import br.com.archat.aplicacao.porta.ConexaoWhatsApp.GrupoWhatsApp;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.List;

/** Conecta (via QR Code), consulta e desconecta o número de WhatsApp da plataforma. */
public class GerenciarConexaoWhatsAppUseCase {

    private final ConexaoWhatsApp conexao;

    public GerenciarConexaoWhatsAppUseCase(ConexaoWhatsApp conexao) {
        this.conexao = conexao;
    }

    public SituacaoConexao situacao() {
        return conexao.situacao();
    }

    public String qrCode() {
        if (conexao.situacao().conectado()) {
            throw new RegraDeNegocioException("O WhatsApp já está conectado");
        }
        return conexao.qrCode();
    }

    public void desconectar() {
        conexao.desconectar();
    }

    public List<GrupoWhatsApp> listarGrupos(int pagina, int tamanhoPagina) {
        if (pagina < 1 || tamanhoPagina < 1 || tamanhoPagina > 100) {
            throw new RegraDeNegocioException("Use página a partir de 1 e tamanho de 1 a 100");
        }
        return conexao.listarGrupos(pagina, tamanhoPagina);
    }
}

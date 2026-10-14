package br.com.archat.aplicacao.whatsapp;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp;
import br.com.archat.aplicacao.porta.ConexaoWhatsApp.SituacaoConexao;
import br.com.archat.aplicacao.porta.ConexaoWhatsApp.GrupoWhatsApp;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.List;

/**
 * Conecta (via QR Code ou código pelo telefone), consulta e desconecta o número de WhatsApp da plataforma.
 * Desconectar ou conectar outro número não apaga nada: conversas, mensagens e atendimentos ficam no banco do
 * sistema e continuam visíveis; as novas mensagens seguem pelo número conectado.
 */
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

    public String codigoPorTelefone(String telefone) {
        String numero = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (numero.length() < 10 || numero.length() > 13) {
            throw new RegraDeNegocioException("Informe o número do WhatsApp a conectar, com DDD (ex.: 5544999999999)");
        }
        if (conexao.situacao().conectado()) {
            throw new RegraDeNegocioException("O WhatsApp já está conectado");
        }
        return conexao.codigoPorTelefone(numero);
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

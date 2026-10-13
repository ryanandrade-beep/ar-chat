package br.com.archat.infraestrutura.envio;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "simulado", matchIfMissing = true)
class ConexaoWhatsAppSimulada implements ConexaoWhatsApp {

    @Override
    public SituacaoConexao situacao() {
        return new SituacaoConexao("simulado", true, true, "Envio simulado: nenhuma conexão real é necessária");
    }

    @Override
    public String qrCode() {
        throw new RegraDeNegocioException("O envio está em modo simulado; configure archat.envio.provedor=zapi");
    }

    @Override
    public List<GrupoWhatsApp> listarGrupos(int pagina, int tamanhoPagina) {
        return List.of();
    }

    @Override
    public void desconectar() {
        throw new RegraDeNegocioException("O envio está em modo simulado; não há conexão para desfazer");
    }
}

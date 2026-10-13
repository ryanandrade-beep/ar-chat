package br.com.archat.infraestrutura.envio.zapi;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "zapi")
class ConexaoWhatsAppZApi implements ConexaoWhatsApp {

    private final ClienteZApi zapi;

    ConexaoWhatsAppZApi(ClienteZApi zapi) {
        this.zapi = zapi;
    }

    @Override
    public SituacaoConexao situacao() {
        ClienteZApi.RespostaStatus status = zapi.status();
        return new SituacaoConexao("zapi", Boolean.TRUE.equals(status.connected()),
                Boolean.TRUE.equals(status.smartphoneConnected()), status.error());
    }

    /** A Z-API devolve {@code value} com a imagem em base64 ou, em aparelhos com passkey, um {@code challenge}. */
    @Override
    public String qrCode() {
        Map<String, Object> resposta = zapi.qrCodeImagem();
        Object imagem = resposta == null ? null : resposta.get("value");
        if (imagem == null) {
            throw new RegraDeNegocioException("A Z-API não devolveu o QR Code (o aparelho pode exigir passkey); "
                    + "conecte pelo painel da Z-API");
        }
        String base64 = imagem.toString();
        return base64.startsWith("data:") ? base64 : "data:image/png;base64," + base64;
    }

    @Override
    public void desconectar() {
        zapi.desconectar();
    }

    @Override
    public List<GrupoWhatsApp> listarGrupos(int pagina, int tamanhoPagina) {
        return zapi.grupos(pagina, tamanhoPagina).stream()
                .filter(g -> !Boolean.FALSE.equals(g.isGroup()))
                .map(g -> new GrupoWhatsApp(g.phone(), g.name()))
                .toList();
    }
}

package br.com.archat.dominio;

import br.com.archat.dominio.campanha.Campanha;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.Midia;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.TipoMidia;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MidiaTest {

    private final Instant agora = Instant.now();

    @Test
    void extensaoDoDocumentoVemDoNomeOuDoLink() {
        assertThat(new Midia(TipoMidia.DOCUMENTO, "https://mercado.com/arquivo", "Encarte.PDF").extensao()).isEqualTo("pdf");
        assertThat(new Midia(TipoMidia.DOCUMENTO, "https://mercado.com/tabela.xlsx?v=2", null).extensao()).isEqualTo("xlsx");
    }

    @Test
    void documentoSemExtensaoELinkInvalidoSaoRecusados() {
        assertThatThrownBy(() -> new Midia(TipoMidia.DOCUMENTO, "https://mercado.com/arquivo", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Midia(TipoMidia.IMAGEM, "ftp://mercado.com/oferta.png", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Midia(TipoMidia.IMAGEM, " ", null)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void mensagemComMidiaTemLegendaOpcionalEMesmoCusto() {
        Midia encarte = new Midia(TipoMidia.IMAGEM, "https://mercado.com/encarte.jpg", null);
        Mensagem semLegenda = Mensagem.novaDoCliente(UUID.randomUUID(), null, null, encarte, Prioridade.NORMAL, agora);
        Mensagem comLegenda = Mensagem.novaDoCliente(UUID.randomUUID(), null, "Ofertas da semana", encarte,
                Prioridade.NORMAL, agora);

        assertThat(semLegenda.conteudo()).isEmpty();
        assertThat(semLegenda.resumo()).isEqualTo("[Imagem]");
        assertThat(comLegenda.resumo()).isEqualTo("[Imagem] Ofertas da semana");
        assertThat(comLegenda.custo()).isEqualByComparingTo("0.25");
    }

    @Test
    void audioNaoAceitaLegendaETextoSemMidiaContinuaObrigatorio() {
        Midia audio = new Midia(TipoMidia.AUDIO, "https://mercado.com/aviso.ogg", null);
        assertThatThrownBy(() -> Mensagem.novaDoCliente(UUID.randomUUID(), null, "legenda", audio, Prioridade.NORMAL, agora))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Mensagem.novaDoCliente(UUID.randomUUID(), null, " ", null, Prioridade.NORMAL, agora))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void campanhaPodeSerSoUmaImagem() {
        Midia encarte = new Midia(TipoMidia.IMAGEM, "https://mercado.com/encarte.jpg", null);
        Campanha campanha = Campanha.nova(UUID.randomUUID(), "Encarte", null, encarte, Prioridade.NORMAL, 4, agora);

        assertThat(campanha.conteudo()).isEmpty();
        assertThat(campanha.custoTotal()).isEqualByComparingTo("1.00");
        assertThatThrownBy(() -> Campanha.nova(UUID.randomUUID(), "Vazia", " ", null, Prioridade.NORMAL, 4, agora))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}

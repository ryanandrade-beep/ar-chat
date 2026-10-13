package br.com.archat.dominio;

import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.conversa.TipoDestinatario;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DestinatarioTest {

    private final UUID cliente = UUID.randomUUID();

    @Test
    void contatoGuardaSoOsDigitosDoTelefone() {
        Destinatario d = Destinatario.novo(cliente, "Maria", "(44) 99999-0000", false);
        assertThat(d.telefone()).isEqualTo("44999990000");
        assertThat(d.grupo()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"120363019502650977-group", " 120363019502650977-GROUP ", "5511999999999-1623275280"})
    void aceitaIdDeGrupoNosFormatosDoWhatsApp(String id) {
        Destinatario d = Destinatario.novo(cliente, TipoDestinatario.GRUPO, "Clientes VIP", id, true);
        assertThat(d.telefone()).isEqualTo(id.strip().toLowerCase());
        assertThat(d.grupo()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"5544999990000", "grupo-vip", "120363019502650977", ""})
    void recusaIdDeGrupoInvalido(String id) {
        assertThatThrownBy(() -> Destinatario.novo(cliente, TipoDestinatario.GRUPO, "Grupo", id, true))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void recusaTelefoneCurto() {
        assertThatThrownBy(() -> Destinatario.novo(cliente, "Maria", "12345", false))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}

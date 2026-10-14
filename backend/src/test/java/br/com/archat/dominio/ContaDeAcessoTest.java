package br.com.archat.dominio;

import br.com.archat.dominio.cliente.ContaDeAcesso;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaDeAcessoTest {

    private final Instant agora = Instant.parse("2026-10-13T12:00:00Z");

    private ContaDeAcesso conta() {
        return ContaDeAcesso.nova(UUID.randomUUID(), " Contato@Mercado.COM ", "(44) 99999-0000", "hash", true, "2026-10", agora);
    }

    @Test
    void normalizaEmailETelefoneEExigeOsTermos() {
        ContaDeAcesso conta = conta();
        assertThat(conta.email()).isEqualTo("contato@mercado.com");
        assertThat(conta.telefone()).isEqualTo("44999990000");
        assertThatThrownBy(() -> ContaDeAcesso.nova(UUID.randomUUID(), "a@b.com", "44999990000", "h", false, "v", agora))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("Termos");
        assertThatThrownBy(() -> ContaDeAcesso.normalizarEmail("sem-arroba")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void senhaPrecisaDeOitoCaracteresComLetrasENumeros() {
        ContaDeAcesso.validarSenha("mercado2026");
        assertThatThrownBy(() -> ContaDeAcesso.validarSenha("curta1")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> ContaDeAcesso.validarSenha("somenteletras")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> ContaDeAcesso.validarSenha("12345678")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void codigoDeRecuperacaoValeUmaVezPorQuinzeMinutos() {
        ContaDeAcesso conta = conta();
        conta.registrarCodigoDeRecuperacao("hash-codigo", agora);
        conta.redefinirSenha(true, "nova", agora.plusSeconds(60));

        assertThat(conta.senhaHash()).isEqualTo("nova");
        assertThatThrownBy(() -> conta.redefinirSenha(true, "outra", agora.plusSeconds(61)))
                .isInstanceOf(RegraDeNegocioException.class);

        conta.registrarCodigoDeRecuperacao("hash-codigo", agora);
        assertThatThrownBy(() -> conta.redefinirSenha(true, "x", agora.plus(ContaDeAcesso.VALIDADE_CODIGO).plusSeconds(1)))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("expirado");
    }

    @Test
    void cincoCodigosErradosInvalidamOCodigo() {
        ContaDeAcesso conta = conta();
        conta.registrarCodigoDeRecuperacao("hash-codigo", agora);
        for (int i = 0; i < ContaDeAcesso.MAXIMO_TENTATIVAS_CODIGO; i++) {
            assertThatThrownBy(() -> conta.redefinirSenha(false, "x", agora)).hasMessage("Código incorreto");
        }
        assertThatThrownBy(() -> conta.redefinirSenha(true, "x", agora)).hasMessageContaining("expirado");
    }
}

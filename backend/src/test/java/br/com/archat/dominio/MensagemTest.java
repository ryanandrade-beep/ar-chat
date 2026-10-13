package br.com.archat.dominio;

import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MensagemTest {

    private final Instant agora = Instant.now();

    private Mensagem nova(Prioridade prioridade) {
        return Mensagem.novaDoCliente(UUID.randomUUID(), "Olá", prioridade, agora);
    }

    @Test
    void custoDependeDaPrioridade() {
        assertThat(nova(Prioridade.NORMAL).custo()).isEqualByComparingTo("0.25");
        assertThat(nova(Prioridade.URGENTE).custo()).isEqualByComparingTo("0.50");
    }

    @Test
    void cicloDeVidaCompleto() {
        Mensagem mensagem = nova(Prioridade.NORMAL);
        mensagem.iniciarProcessamento(agora);
        mensagem.confirmarEnvio("EXT-1", agora);
        mensagem.confirmarEntrega(agora);
        mensagem.confirmarLeitura(agora);
        assertThat(mensagem.status()).isEqualTo(StatusMensagem.LIDA);
    }

    @Test
    void voltaParaFilaAteEsgotarAsTentativas() {
        Mensagem mensagem = nova(Prioridade.NORMAL);
        for (int tentativa = 1; tentativa < 3; tentativa++) {
            mensagem.iniciarProcessamento(agora);
            mensagem.registrarFalhaDeEnvio(3, agora);
            assertThat(mensagem.status()).isEqualTo(StatusMensagem.ENFILEIRADA);
        }
        mensagem.iniciarProcessamento(agora);
        mensagem.registrarFalhaDeEnvio(3, agora);
        assertThat(mensagem.falhouDefinitivamente()).isTrue();
        assertThat(mensagem.tentativas()).isEqualTo(3);
    }

    @Test
    void naoPermiteTransicaoInvalida() {
        Mensagem mensagem = nova(Prioridade.NORMAL);
        assertThatThrownBy(() -> mensagem.confirmarLeitura(agora)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void recusaConteudoEmBranco() {
        assertThatThrownBy(() -> Mensagem.novaDoCliente(UUID.randomUUID(), "  ", Prioridade.NORMAL, agora))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}

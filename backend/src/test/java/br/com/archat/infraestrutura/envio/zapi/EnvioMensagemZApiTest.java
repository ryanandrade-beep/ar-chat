package br.com.archat.infraestrutura.envio.zapi;

import br.com.archat.dominio.mensagem.Midia;
import br.com.archat.dominio.mensagem.TipoMidia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EnvioMensagemZApiTest {

    @ParameterizedTest
    @CsvSource({
            "44999990000, 5544999990000",
            "4499990000, 554499990000",
            "5544999990000, 5544999990000",
            "120363019502650977-group, 120363019502650977-group",
            "5511999999999-1623275280, 5511999999999-1623275280"})
    void acrescentaDdiSoEmTelefoneSemDdiENuncaEmGrupo(String cadastrado, String enviado) {
        assertThat(EnvioMensagemZApi.comDdi(cadastrado)).isEqualTo(enviado);
    }

    @Test
    void cadaTipoDeMensagemUsaASuaRotaDaZApi() {
        assertThat(EnvioMensagemZApi.rota(null)).isEqualTo("send-text");
        assertThat(EnvioMensagemZApi.rota(new Midia(TipoMidia.IMAGEM, "https://x.com/a.jpg", null))).isEqualTo("send-image");
        assertThat(EnvioMensagemZApi.rota(new Midia(TipoMidia.DOCUMENTO, "https://x.com/encarte.pdf", null)))
                .isEqualTo("send-document/pdf");
        assertThat(EnvioMensagemZApi.rota(new Midia(TipoMidia.AUDIO, "https://x.com/a.ogg", null))).isEqualTo("send-audio");
        assertThat(EnvioMensagemZApi.rota(new Midia(TipoMidia.VIDEO, "https://x.com/a.mp4", null))).isEqualTo("send-video");
    }
}

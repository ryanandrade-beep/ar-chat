package br.com.archat.infraestrutura.envio.zapi;

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
}

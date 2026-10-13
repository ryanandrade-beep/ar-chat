package br.com.archat.dominio;

import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

    @ParameterizedTest
    @CsvSource({"529.982.247-25, 52998224725, CPF", "11.222.333/0001-81, 11222333000181, CNPJ"})
    void aceitaDocumentoValidoENormaliza(String informado, String esperado, TipoDocumento tipo) {
        assertThat(new Documento(informado, tipo).numero()).isEqualTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "11111111111", "123", ""})
    void recusaCpfInvalido(String numero) {
        assertThatThrownBy(() -> new Documento(numero, TipoDocumento.CPF)).isInstanceOf(RegraDeNegocioException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000182", "00000000000000", "52998224725"})
    void recusaCnpjInvalido(String numero) {
        assertThatThrownBy(() -> new Documento(numero, TipoDocumento.CNPJ)).isInstanceOf(RegraDeNegocioException.class);
    }
}

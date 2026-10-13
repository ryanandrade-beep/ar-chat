package br.com.archat.dominio;

import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.excecao.SaldoInsuficienteException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private static final Documento CPF = new Documento("52998224725", TipoDocumento.CPF);

    static Cliente prePago(String saldo) {
        return Cliente.novo("Empresa", CPF, TipoPlano.PRE_PAGO, new BigDecimal(saldo), Instant.now());
    }

    static Cliente posPago(String limite) {
        return Cliente.novo("Empresa", CPF, TipoPlano.POS_PAGO, new BigDecimal(limite), Instant.now());
    }

    @Nested
    class PrePago {

        @Test
        void cincoMensagensNormaisComDezReaisDeixamOitoESetentaECinco() {
            Cliente cliente = prePago("10.00");
            for (int i = 0; i < 5; i++) {
                cliente.cobrar(new BigDecimal("0.25"));
            }
            assertThat(cliente.saldo()).isEqualByComparingTo("8.75");
        }

        @Test
        void recusaQuandoSaldoNaoCobreOCusto() {
            Cliente cliente = prePago("0.40");
            assertThatThrownBy(() -> cliente.cobrar(new BigDecimal("0.50"))).isInstanceOf(SaldoInsuficienteException.class);
            assertThat(cliente.saldo()).isEqualByComparingTo("0.40");
        }

        @Test
        void estornoDevolveOSaldo() {
            Cliente cliente = prePago("1.00");
            cliente.cobrar(new BigDecimal("0.50"));
            cliente.estornar(new BigDecimal("0.50"));
            assertThat(cliente.saldo()).isEqualByComparingTo("1.00");
        }

        @Test
        void naoAceitaAjusteDeLimite() {
            assertThatThrownBy(() -> prePago("1.00").ajustarLimite(BigDecimal.TEN)).isInstanceOf(RegraDeNegocioException.class);
        }
    }

    @Nested
    class PosPago {

        @Test
        void limiteDeCinquentaComQuarentaUsadosDeixaSeteECinquenta() {
            Cliente cliente = posPago("50.00");
            for (int i = 0; i < 160; i++) {
                cliente.cobrar(new BigDecimal("0.25"));
            }
            for (int i = 0; i < 10; i++) {
                cliente.cobrar(new BigDecimal("0.25"));
            }
            assertThat(cliente.disponivel()).isEqualByComparingTo("7.50");
        }

        @Test
        void recusaQuandoUltrapassaOLimite() {
            Cliente cliente = posPago("0.50");
            cliente.cobrar(new BigDecimal("0.50"));
            assertThatThrownBy(() -> cliente.cobrar(new BigDecimal("0.25"))).isInstanceOf(SaldoInsuficienteException.class);
        }

        @Test
        void limiteNaoPodeFicarAbaixoDoConsumo() {
            Cliente cliente = posPago("10.00");
            cliente.cobrar(new BigDecimal("0.50"));
            assertThatThrownBy(() -> cliente.ajustarLimite(new BigDecimal("0.25"))).isInstanceOf(RegraDeNegocioException.class);
        }
    }

    @Nested
    class ConversaoDePlano {

        @Test
        void saldoDoPrePagoViraCreditoNoPosPago() {
            Cliente cliente = prePago("3.00");
            cliente.converterPara(TipoPlano.POS_PAGO, new BigDecimal("10.00"));
            assertThat(cliente.disponivel()).isEqualByComparingTo("13.00");
        }

        @Test
        void posPagoComConsumoPendenteNaoViraPrePago() {
            Cliente cliente = posPago("10.00");
            cliente.cobrar(new BigDecimal("0.25"));
            assertThatThrownBy(() -> cliente.converterPara(TipoPlano.PRE_PAGO, null)).isInstanceOf(RegraDeNegocioException.class);
        }
    }

    @Test
    void clienteInativoNaoEnvia() {
        Cliente cliente = prePago("10.00");
        cliente.inativar();
        assertThatThrownBy(() -> cliente.cobrar(new BigDecimal("0.25"))).isInstanceOf(RegraDeNegocioException.class);
    }
}

package br.com.archat.aplicacao;

import br.com.archat.aplicacao.campanha.DispararCampanhaUseCase;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.excecao.SaldoInsuficienteException;
import br.com.archat.dominio.mensagem.Prioridade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampanhaUseCaseTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);
    private final Fakes.Clientes clientes = new Fakes.Clientes();
    private final Fakes.Destinatarios destinatarios = new Fakes.Destinatarios();
    private final Fakes.Conversas conversas = new Fakes.Conversas();
    private final Fakes.Mensagens mensagens = new Fakes.Mensagens();
    private final Fakes.Transacoes transacoes = new Fakes.Transacoes();
    private final Fakes.Campanhas campanhas = new Fakes.Campanhas();
    private final Fakes.Fila fila = new Fakes.Fila();

    private final DispararCampanhaUseCase disparar = new DispararCampanhaUseCase(clientes, destinatarios, conversas,
            mensagens, transacoes, campanhas, fila, Fakes.SEM_TRANSACAO, relogio);

    private Cliente supermercado(String saldo) {
        Cliente cliente = Cliente.novo("Supermercado São Pedro", new Documento("11222333000181", TipoDocumento.CNPJ),
                TipoPlano.PRE_PAGO, new BigDecimal(saldo), relogio.instant());
        clientes.salvar(cliente);
        return cliente;
    }

    private Destinatario contato(Cliente cliente, String nome, String telefone, boolean aceitaOfertas) {
        Destinatario destinatario = Destinatario.novo(cliente.id(), nome, telefone, aceitaOfertas);
        destinatarios.salvar(destinatario);
        return destinatario;
    }

    private DispararCampanhaUseCase.Comando paraTodos(Cliente cliente) {
        return new DispararCampanhaUseCase.Comando(cliente.id(), "Ofertas da semana", "Picanha R$ 49,90/kg",
                Prioridade.NORMAL, null, true);
    }

    @Test
    void enviaSomenteParaQuemAceitaOfertasECobraCadaMensagem() {
        Cliente cliente = supermercado("10.00");
        contato(cliente, "Ana", "44999990001", true);
        contato(cliente, "Bruno", "44999990002", true);
        contato(cliente, "Carla", "44999990003", false);

        var disparada = disparar.executar(paraTodos(cliente));

        assertThat(disparada.campanha().totalDestinatarios()).isEqualTo(2);
        assertThat(disparada.campanha().custoTotal()).isEqualByComparingTo("0.50");
        assertThat(disparada.disponivel()).isEqualByComparingTo("9.50");
        assertThat(fila.itens).hasSize(2);
        assertThat(transacoes.dados).hasSize(2);
        assertThat(mensagens.dados.values()).allSatisfy(m ->
                assertThat(m.campanhaId()).isEqualTo(disparada.campanha().id()));
        assertThat(conversas.dados).hasSize(2);
    }

    @Test
    void semSaldoParaTodaACampanhaNadaEhCobrado() {
        Cliente cliente = supermercado("0.50");
        for (int i = 0; i < 3; i++) {
            contato(cliente, "Contato " + i, "4499999000" + i, true);
        }

        assertThatThrownBy(() -> disparar.executar(paraTodos(cliente))).isInstanceOf(SaldoInsuficienteException.class);
        assertThat(clientes.buscarPorId(cliente.id()).orElseThrow().saldo()).isEqualByComparingTo("0.50");
        assertThat(fila.itens).isEmpty();
        assertThat(transacoes.dados).isEmpty();
        assertThat(campanhas.dados).isEmpty();
    }

    @Test
    void recusaContatoEscolhidoQueNaoAceitaOfertas() {
        Cliente cliente = supermercado("10.00");
        Destinatario semConsentimento = contato(cliente, "Carla", "44999990003", false);

        assertThatThrownBy(() -> disparar.executar(new DispararCampanhaUseCase.Comando(cliente.id(), "Ofertas",
                "Promoção", Prioridade.NORMAL, List.of(semConsentimento.id()), false)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("LGPD");
    }

    @Test
    void recusaCampanhaSemDestinatarios() {
        Cliente cliente = supermercado("10.00");
        assertThatThrownBy(() -> disparar.executar(paraTodos(cliente))).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void ignoraContatoRepetidoNaLista() {
        Cliente cliente = supermercado("10.00");
        Destinatario ana = contato(cliente, "Ana", "44999990001", true);

        var disparada = disparar.executar(new DispararCampanhaUseCase.Comando(cliente.id(), "Ofertas", "Promoção",
                Prioridade.URGENTE, List.of(ana.id(), ana.id()), false));

        assertThat(disparada.campanha().totalDestinatarios()).isEqualTo(1);
        assertThat(disparada.campanha().custoTotal()).isEqualByComparingTo("0.50");
    }

    @Test
    void naoUsaContatoDeOutroCliente() {
        Cliente cliente = supermercado("10.00");
        Destinatario deOutro = Destinatario.novo(UUID.randomUUID(), "Zé", "44999990009", true);
        destinatarios.salvar(deOutro);

        assertThatThrownBy(() -> disparar.executar(new DispararCampanhaUseCase.Comando(cliente.id(), "Ofertas",
                "Promoção", Prioridade.NORMAL, List.of(deOutro.id()), false))).hasMessageContaining("não encontrado");
    }
}

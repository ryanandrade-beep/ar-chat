package br.com.archat.aplicacao;

import br.com.archat.aplicacao.atendimento.RegistroDeAtendimento;
import br.com.archat.aplicacao.mensagem.EnviarMensagemUseCase;
import br.com.archat.aplicacao.mensagem.ProcessarMensagemUseCase;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.excecao.SaldoInsuficienteException;
import br.com.archat.dominio.financeiro.TipoTransacao;
import br.com.archat.dominio.financeiro.Transacao;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MensagemUseCasesTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneOffset.UTC);
    private final Fakes.Clientes clientes = new Fakes.Clientes();
    private final Fakes.Destinatarios destinatarios = new Fakes.Destinatarios();
    private final Fakes.Conversas conversas = new Fakes.Conversas();
    private final Fakes.Mensagens mensagens = new Fakes.Mensagens();
    private final Fakes.Transacoes transacoes = new Fakes.Transacoes();
    private final Fakes.Fila fila = new Fakes.Fila();
    private final Fakes.Metricas metricas = new Fakes.Metricas();
    private final Fakes.Atendimentos atendimentos = new Fakes.Atendimentos();

    private Cliente cliente;
    private Destinatario destinatario;

    @BeforeEach
    void preparar() {
        cliente = Cliente.novo("Empresa ABC", new Documento("11222333000181", TipoDocumento.CNPJ), TipoPlano.PRE_PAGO,
                new BigDecimal("1.00"), relogio.instant());
        clientes.salvar(cliente);
        destinatario = Destinatario.novo(cliente.id(), "Maria Silva", "44999990000", false);
        destinatarios.salvar(destinatario);
    }

    private EnviarMensagemUseCase enviar() {
        return new EnviarMensagemUseCase(clientes, destinatarios, conversas, mensagens, transacoes, fila,
                Fakes.SEM_TRANSACAO, relogio, Duration.ofSeconds(1), new RegistroDeAtendimento(atendimentos));
    }

    private ProcessarMensagemUseCase processar(Fakes.EnvioProgramado canal) {
        return new ProcessarMensagemUseCase(mensagens, conversas, destinatarios, clientes, transacoes, fila, canal,
                metricas, Fakes.SEM_TRANSACAO, relogio, 3);
    }

    private Mensagem enviarPara(Prioridade prioridade) {
        return enviar().executar(new EnviarMensagemUseCase.Comando(cliente.id(), null, destinatario.id(), "Olá",
                prioridade)).mensagem();
    }

    @Test
    void envioCobraRegistraTransacaoEEnfileira() {
        var enviada = enviar().executar(new EnviarMensagemUseCase.Comando(cliente.id(), null, destinatario.id(),
                "Olá, como vai?", Prioridade.URGENTE));

        assertThat(enviada.disponivel()).isEqualByComparingTo("0.50");
        assertThat(enviada.mensagem().status()).isEqualTo(StatusMensagem.ENFILEIRADA);
        assertThat(fila.itens).containsExactly(enviada.mensagem().id());
        assertThat(transacoes.dados).singleElement().extracting(Transacao::tipo).isEqualTo(TipoTransacao.DEBITO);
        assertThat(conversas.buscarPorDestinatario(destinatario.id())).isPresent();
    }

    @Test
    void semSaldoNadaEhCobradoNemEnfileirado() {
        enviarPara(Prioridade.URGENTE);
        enviarPara(Prioridade.URGENTE);

        assertThatThrownBy(() -> enviarPara(Prioridade.NORMAL)).isInstanceOf(SaldoInsuficienteException.class);
        assertThat(clientes.buscarPorId(cliente.id()).orElseThrow().saldo()).isEqualByComparingTo("0.00");
        assertThat(fila.itens).hasSize(2);
        assertThat(transacoes.dados).hasSize(2);
    }

    @Test
    void naoEnviaParaDestinatarioDeOutroCliente() {
        Destinatario deOutro = Destinatario.novo(UUID.randomUUID(), "João", "44988887777", false);
        destinatarios.salvar(deOutro);

        assertThatThrownBy(() -> enviar().executar(new EnviarMensagemUseCase.Comando(cliente.id(), null, deOutro.id(),
                "Oi", Prioridade.NORMAL))).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void processamentoComSucessoMarcaComoEnviadaEUsaOTelefoneDoDestinatario() {
        Mensagem mensagem = enviarPara(Prioridade.NORMAL);
        Fakes.EnvioProgramado canal = new Fakes.EnvioProgramado().responder(true);

        assertThat(processar(canal).executar(mensagem.id())).contains(StatusMensagem.ENVIADA);
        assertThat(canal.recebidos).singleElement().satisfies(envio -> {
            assertThat(envio.telefone()).isEqualTo("44999990000");
            assertThat(envio.tentativa()).isEqualTo(1);
        });
        assertThat(metricas.enviadas).isEqualTo(1);
    }

    @Test
    void falhaTemporariaDevolveParaAFila() {
        Mensagem mensagem = enviarPara(Prioridade.NORMAL);
        fila.itens.clear();

        assertThat(processar(new Fakes.EnvioProgramado().responder(false)).executar(mensagem.id()))
                .contains(StatusMensagem.ENFILEIRADA);
        assertThat(fila.itens).containsExactly(mensagem.id());
        assertThat(metricas.temporarias).isEqualTo(1);
    }

    @Test
    void terceiraFalhaEstornaOValor() {
        Mensagem mensagem = enviarPara(Prioridade.URGENTE);
        ProcessarMensagemUseCase processarMensagem = processar(new Fakes.EnvioProgramado().responder(false, false, false));

        processarMensagem.executar(mensagem.id());
        processarMensagem.executar(mensagem.id());
        assertThat(processarMensagem.executar(mensagem.id())).contains(StatusMensagem.FALHA);

        assertThat(clientes.buscarPorId(cliente.id()).orElseThrow().saldo()).isEqualByComparingTo("1.00");
        assertThat(transacoes.dados).extracting(Transacao::tipo)
                .containsExactly(TipoTransacao.DEBITO, TipoTransacao.ESTORNO);
        assertThat(metricas.definitivas).isEqualTo(1);
    }

    @Test
    void ignoraMensagemQueNaoEstaMaisNaFila() {
        Mensagem mensagem = enviarPara(Prioridade.NORMAL);
        ProcessarMensagemUseCase processarMensagem = processar(new Fakes.EnvioProgramado().responder(true));
        processarMensagem.executar(mensagem.id());

        assertThat(processarMensagem.executar(mensagem.id())).isEmpty();
    }
}

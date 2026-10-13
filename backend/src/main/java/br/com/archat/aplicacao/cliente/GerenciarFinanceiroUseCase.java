package br.com.archat.aplicacao.cliente;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.TransacaoRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.financeiro.Transacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Function;

/** Créditos, limites e conversão de plano. Toda alteração gera uma transação no histórico. */
public class GerenciarFinanceiroUseCase {

    private final ClienteRepositorio clientes;
    private final TransacaoRepositorio transacoes;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public GerenciarFinanceiroUseCase(ClienteRepositorio clientes, TransacaoRepositorio transacoes,
                                      UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.clientes = clientes;
        this.transacoes = transacoes;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public Cliente adicionarCredito(UUID clienteId, BigDecimal valor) {
        return alterar(clienteId, cliente -> {
            cliente.adicionarCredito(valor);
            return Transacao.credito(cliente, valor, relogio.instant());
        });
    }

    public Cliente ajustarLimite(UUID clienteId, BigDecimal novoLimite) {
        return alterar(clienteId, cliente -> {
            cliente.ajustarLimite(novoLimite);
            return Transacao.ajusteDeLimite(cliente, relogio.instant());
        });
    }

    public Cliente converterPlano(UUID clienteId, TipoPlano novoPlano, BigDecimal limiteMensal) {
        return alterar(clienteId, cliente -> {
            cliente.converterPara(novoPlano, limiteMensal);
            return novoPlano == TipoPlano.POS_PAGO
                    ? Transacao.ajusteDeLimite(cliente, relogio.instant())
                    : Transacao.credito(cliente, cliente.saldo(), relogio.instant());
        });
    }

    private Cliente alterar(UUID clienteId, Function<Cliente, Transacao> alteracao) {
        return unidadeDeTrabalho.executar(() -> {
            Cliente cliente = clientes.buscarPorIdParaAtualizacao(clienteId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente"));
            Transacao transacao = alteracao.apply(cliente);
            clientes.salvar(cliente);
            transacoes.salvar(transacao);
            return cliente;
        });
    }
}

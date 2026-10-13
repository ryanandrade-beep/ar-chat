package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.financeiro.TipoTransacao;
import br.com.archat.dominio.financeiro.Transacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoRepositorio {

    void salvar(Transacao transacao);

    /** Transações do período [de, ate); com {@code clienteId} nulo, traz as de todos os clientes. */
    List<Transacao> listar(UUID clienteId, Instant de, Instant ate);

    /** Última transação do cliente antes do instante — dá o saldo/limite disponível naquele momento. */
    Optional<Transacao> ultimaAntesDe(UUID clienteId, Instant instante);

    /** Quantidade e soma das transações do período [de, ate), por cliente e tipo. */
    List<TotalPorCliente> totalizarPorCliente(Instant de, Instant ate);

    record TotalPorCliente(UUID clienteId, TipoTransacao tipo, long quantidade, BigDecimal valor) {
    }
}

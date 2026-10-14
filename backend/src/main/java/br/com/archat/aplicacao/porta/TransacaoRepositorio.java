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

    List<Transacao> listar(UUID clienteId, Instant de, Instant ate);

    Optional<Transacao> ultimaAntesDe(UUID clienteId, Instant instante);

    List<TotalPorCliente> totalizarPorCliente(Instant de, Instant ate);

    record TotalPorCliente(UUID clienteId, TipoTransacao tipo, long quantidade, BigDecimal valor) {
    }
}

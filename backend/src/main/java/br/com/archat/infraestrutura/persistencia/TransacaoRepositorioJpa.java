package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.TransacaoRepositorio;
import br.com.archat.dominio.financeiro.TipoTransacao;
import br.com.archat.dominio.financeiro.Transacao;
import br.com.archat.infraestrutura.persistencia.entidade.TransacaoEntity;
import br.com.archat.infraestrutura.persistencia.jpa.TransacaoJpa;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class TransacaoRepositorioJpa implements TransacaoRepositorio {

    private final TransacaoJpa jpa;

    TransacaoRepositorioJpa(TransacaoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Transacao t) {
        jpa.save(new TransacaoEntity(t.id(), t.clienteId(), t.mensagemId(), t.tipo(), t.valor(), t.saldoApos(),
                t.dataHora()));
    }

    @Override
    public List<Transacao> listar(UUID clienteId, Instant de, Instant ate) {
        List<TransacaoEntity> linhas = clienteId == null
                ? jpa.findByDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(de, ate)
                : jpa.findByClienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(clienteId, de, ate);
        return linhas.stream().map(TransacaoRepositorioJpa::paraDominio).toList();
    }

    @Override
    public Optional<Transacao> ultimaAntesDe(UUID clienteId, Instant instante) {
        return jpa.findFirstByClienteIdAndDataHoraLessThanOrderByDataHoraDesc(clienteId, instante)
                .map(TransacaoRepositorioJpa::paraDominio);
    }

    @Override
    public List<TotalPorCliente> totalizarPorCliente(Instant de, Instant ate) {
        return jpa.totalizarPorCliente(de, ate).stream()
                .map(l -> new TotalPorCliente((UUID) l[0], (TipoTransacao) l[1], (Long) l[2], (BigDecimal) l[3]))
                .toList();
    }

    private static Transacao paraDominio(TransacaoEntity e) {
        return new Transacao(e.getId(), e.getClienteId(), e.getMensagemId(), e.getTipo(), e.getValor(),
                e.getSaldoApos(), e.getDataHora());
    }
}

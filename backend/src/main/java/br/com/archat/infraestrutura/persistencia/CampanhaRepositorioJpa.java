package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.CampanhaRepositorio;
import br.com.archat.dominio.campanha.Campanha;
import br.com.archat.infraestrutura.persistencia.entidade.CampanhaEntity;
import br.com.archat.infraestrutura.persistencia.jpa.CampanhaJpa;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class CampanhaRepositorioJpa implements CampanhaRepositorio {

    private final CampanhaJpa jpa;

    CampanhaRepositorioJpa(CampanhaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Campanha c) {
        CampanhaEntity entidade = new CampanhaEntity(c.id(), c.clienteId(), c.nome(), c.conteudo(), c.prioridade(),
                c.totalDestinatarios(), c.custoTotal(), c.criadaEm());
        entidade.definirMidia(c.midia());
        jpa.save(entidade);
    }

    @Override
    public Optional<Campanha> buscarPorId(UUID id) {
        return jpa.findById(id).map(CampanhaRepositorioJpa::paraDominio);
    }

    @Override
    public List<Campanha> listarPorCliente(UUID clienteId) {
        return jpa.findByClienteIdOrderByCriadaEmDesc(clienteId).stream().map(CampanhaRepositorioJpa::paraDominio).toList();
    }

    @Override
    public List<Campanha> listarPorPeriodo(UUID clienteId, Instant de, Instant ate) {
        List<CampanhaEntity> linhas = clienteId == null
                ? jpa.findByCriadaEmGreaterThanEqualAndCriadaEmLessThanOrderByCriadaEmDesc(de, ate)
                : jpa.findByClienteIdAndCriadaEmGreaterThanEqualAndCriadaEmLessThanOrderByCriadaEmDesc(clienteId, de, ate);
        return linhas.stream().map(CampanhaRepositorioJpa::paraDominio).toList();
    }

    private static Campanha paraDominio(CampanhaEntity e) {
        return new Campanha(e.getId(), e.getClienteId(), e.getNome(), e.getConteudo(), e.getMidia(), e.getPrioridade(),
                e.getTotalDestinatarios(), e.getCustoTotal(), e.getCriadaEm());
    }
}

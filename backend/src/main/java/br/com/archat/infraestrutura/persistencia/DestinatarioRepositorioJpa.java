package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.DestinatarioRepositorio;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.infraestrutura.persistencia.entidade.DestinatarioEntity;
import br.com.archat.infraestrutura.persistencia.jpa.DestinatarioJpa;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class DestinatarioRepositorioJpa implements DestinatarioRepositorio {

    private final DestinatarioJpa jpa;

    DestinatarioRepositorioJpa(DestinatarioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Destinatario d) {
        jpa.save(new DestinatarioEntity(d.id(), d.clienteId(), d.tipo(), d.nome(), d.telefone(), d.aceitaOfertas()));
    }

    @Override
    public Optional<Destinatario> buscarPorId(UUID id) {
        return jpa.findById(id).map(DestinatarioRepositorioJpa::paraDominio);
    }

    @Override
    public List<Destinatario> buscarPorIds(Collection<UUID> ids) {
        return jpa.findAllById(ids).stream().map(DestinatarioRepositorioJpa::paraDominio).toList();
    }

    @Override
    public List<Destinatario> listarPorCliente(UUID clienteId) {
        return jpa.findByClienteIdOrderByNomeAsc(clienteId).stream()
                .map(DestinatarioRepositorioJpa::paraDominio).toList();
    }

    @Override
    public Optional<Destinatario> buscarPorClienteETelefone(UUID clienteId, String telefone) {
        return jpa.findByClienteIdAndTelefone(clienteId, telefone).map(DestinatarioRepositorioJpa::paraDominio);
    }

    @Override
    public List<Destinatario> listarQueAceitamOfertas(UUID clienteId) {
        return jpa.findByClienteIdAndAceitaOfertasTrueOrderByNomeAsc(clienteId).stream()
                .map(DestinatarioRepositorioJpa::paraDominio).toList();
    }

    @Override
    public List<Destinatario> buscarPorTelefones(Collection<String> telefones) {
        return telefones.isEmpty() ? List.of()
                : jpa.findByTelefoneIn(telefones).stream().map(DestinatarioRepositorioJpa::paraDominio).toList();
    }

    @Override
    public void excluir(UUID id) {
        jpa.deleteById(id);
    }

    private static Destinatario paraDominio(DestinatarioEntity e) {
        return new Destinatario(e.getId(), e.getClienteId(), e.getTipo(), e.getNome(), e.getTelefone(), e.isAceitaOfertas());
    }
}

package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.conversa.Destinatario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DestinatarioRepositorio {

    void salvar(Destinatario destinatario);

    Optional<Destinatario> buscarPorId(UUID id);

    List<Destinatario> buscarPorIds(Collection<UUID> ids);

    List<Destinatario> listarPorCliente(UUID clienteId);

    Optional<Destinatario> buscarPorClienteETelefone(UUID clienteId, String telefone);

    List<Destinatario> listarQueAceitamOfertas(UUID clienteId);

    List<Destinatario> buscarPorTelefones(Collection<String> telefones);

    void excluir(UUID id);
}

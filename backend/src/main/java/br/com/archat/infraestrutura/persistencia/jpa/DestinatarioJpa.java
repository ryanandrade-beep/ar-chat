package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.DestinatarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DestinatarioJpa extends JpaRepository<DestinatarioEntity, UUID> {

    List<DestinatarioEntity> findByClienteIdOrderByNomeAsc(UUID clienteId);

    Optional<DestinatarioEntity> findByClienteIdAndTelefone(UUID clienteId, String telefone);

    List<DestinatarioEntity> findByTelefoneIn(Collection<String> telefones);

    List<DestinatarioEntity> findByClienteIdAndAceitaOfertasTrueOrderByNomeAsc(UUID clienteId);
}

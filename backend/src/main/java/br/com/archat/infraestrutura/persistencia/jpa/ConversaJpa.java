package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.ConversaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversaJpa extends JpaRepository<ConversaEntity, UUID> {

    Optional<ConversaEntity> findByDestinatarioId(UUID destinatarioId);

    List<ConversaEntity> findByClienteIdOrderByDataUltimaMensagemDesc(UUID clienteId);
}

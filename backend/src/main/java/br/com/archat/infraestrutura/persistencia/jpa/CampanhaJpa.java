package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.CampanhaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CampanhaJpa extends JpaRepository<CampanhaEntity, UUID> {

    List<CampanhaEntity> findByClienteIdOrderByCriadaEmDesc(UUID clienteId);

    List<CampanhaEntity> findByCriadaEmGreaterThanEqualAndCriadaEmLessThanOrderByCriadaEmDesc(Instant de, Instant ate);

    List<CampanhaEntity> findByClienteIdAndCriadaEmGreaterThanEqualAndCriadaEmLessThanOrderByCriadaEmDesc(
            UUID clienteId, Instant de, Instant ate);
}

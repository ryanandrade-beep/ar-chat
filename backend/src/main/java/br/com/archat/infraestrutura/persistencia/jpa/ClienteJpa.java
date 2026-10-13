package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.infraestrutura.persistencia.entidade.ClienteEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteJpa extends JpaRepository<ClienteEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClienteEntity c where c.id = :id")
    Optional<ClienteEntity> buscarParaAtualizacao(@Param("id") UUID id);

    Optional<ClienteEntity> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    List<ClienteEntity> findAllByOrderByNomeAsc();

    @Modifying
    @Query("update ClienteEntity c set c.consumoMes = 0 where c.tipoPlano = :plano and c.consumoMes > 0")
    int zerarConsumo(@Param("plano") TipoPlano plano);
}

package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdministradorJpa extends JpaRepository<AdministradorEntity, UUID> {

    Optional<AdministradorEntity> findByLogin(String login);

    List<AdministradorEntity> findAllByOrderByNomeAsc();
}

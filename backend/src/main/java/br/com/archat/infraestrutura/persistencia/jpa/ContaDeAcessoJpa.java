package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.ContaDeAcessoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContaDeAcessoJpa extends JpaRepository<ContaDeAcessoEntity, UUID> {

    Optional<ContaDeAcessoEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}

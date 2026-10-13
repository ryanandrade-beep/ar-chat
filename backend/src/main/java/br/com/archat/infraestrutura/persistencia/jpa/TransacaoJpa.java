package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.infraestrutura.persistencia.entidade.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoJpa extends JpaRepository<TransacaoEntity, UUID> {

    List<TransacaoEntity> findByDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(Instant de, Instant ate);

    List<TransacaoEntity> findByClienteIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(
            UUID clienteId, Instant de, Instant ate);

    Optional<TransacaoEntity> findFirstByClienteIdAndDataHoraLessThanOrderByDataHoraDesc(UUID clienteId, Instant instante);

    @Query("""
            select t.clienteId, t.tipo, count(t), sum(t.valor) from TransacaoEntity t
            where t.dataHora >= :de and t.dataHora < :ate
            group by t.clienteId, t.tipo""")
    List<Object[]> totalizarPorCliente(@Param("de") Instant de, @Param("ate") Instant ate);
}

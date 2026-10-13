package br.com.archat.infraestrutura.persistencia.jpa;

import br.com.archat.dominio.mensagem.StatusMensagem;
import br.com.archat.infraestrutura.persistencia.entidade.MensagemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensagemJpa extends JpaRepository<MensagemEntity, UUID>, JpaSpecificationExecutor<MensagemEntity> {

    @Query("""
            select m from MensagemEntity m, ConversaEntity c
            where m.conversaId = c.id and m.id = :mensagemId and c.clienteId = :clienteId""")
    Optional<MensagemEntity> buscarDoCliente(@Param("mensagemId") UUID mensagemId, @Param("clienteId") UUID clienteId);

    Optional<MensagemEntity> findByIdExterno(String idExterno);

    List<MensagemEntity> findByStatusInOrderByCriadaEmAsc(Collection<StatusMensagem> status);

    @Query("""
            select count(m) > 0 from MensagemEntity m, ConversaEntity c
            where m.conversaId = c.id and c.destinatarioId = :destinatarioId""")
    boolean existeParaDestinatario(@Param("destinatarioId") UUID destinatarioId);

    @Query("select m.status, count(m) from MensagemEntity m group by m.status")
    List<Object[]> contarPorStatus();

    @Query("select m.status, count(m) from MensagemEntity m where m.campanhaId = :campanhaId group by m.status")
    List<Object[]> contarPorStatusDaCampanha(@Param("campanhaId") UUID campanhaId);

    @Query("""
            select c.clienteId, m.prioridade, m.status, count(m) from MensagemEntity m, ConversaEntity c
            where m.conversaId = c.id and m.criadaEm >= :de and m.criadaEm < :ate
            group by c.clienteId, m.prioridade, m.status""")
    List<Object[]> contarPorCliente(@Param("de") Instant de, @Param("ate") Instant ate);

    @Query("""
            select m.campanhaId, m.status, count(m) from MensagemEntity m
            where m.campanhaId in :campanhaIds
            group by m.campanhaId, m.status""")
    List<Object[]> contarPorStatusDasCampanhas(@Param("campanhaIds") Collection<UUID> campanhaIds);
}

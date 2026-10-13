package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MensagemRepositorio {

    void salvar(Mensagem mensagem);

    Optional<Mensagem> buscarPorId(UUID id);

    Optional<Mensagem> buscarDoCliente(UUID mensagemId, UUID clienteId);

    /** Mensagem pelo identificador no canal (ex.: messageId da Z-API). */
    Optional<Mensagem> buscarPorIdExterno(String idExterno);

    /** Mensagens que atendem ao filtro, em ordem cronológica. */
    List<Mensagem> listar(FiltroMensagens filtro);

    /** Mensagens enfileiradas ou em processamento, em ordem de chegada. */
    List<Mensagem> listarPendentes();

    boolean existeParaDestinatario(UUID destinatarioId);

    Map<StatusMensagem, Long> contarPorStatus();

    Map<StatusMensagem, Long> contarPorStatusDaCampanha(UUID campanhaId);

    /** Mensagens criadas no período [de, ate), contadas por cliente, prioridade e status. */
    List<ContagemPorCliente> contarPorCliente(Instant de, Instant ate);

    /** Mensagens de cada campanha, contadas por status. Campanhas sem mensagens ficam fora do mapa. */
    Map<UUID, Map<StatusMensagem, Long>> contarPorStatusDasCampanhas(Collection<UUID> campanhaIds);

    record ContagemPorCliente(UUID clienteId, Prioridade prioridade, StatusMensagem status, long quantidade) {
    }
}

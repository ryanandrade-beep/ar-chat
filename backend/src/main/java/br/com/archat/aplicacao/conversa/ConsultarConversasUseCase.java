package br.com.archat.aplicacao.conversa;

import br.com.archat.aplicacao.porta.ConversaRepositorio;
import br.com.archat.aplicacao.porta.DestinatarioRepositorio;
import br.com.archat.dominio.atendimento.StatusAtendimento;
import br.com.archat.dominio.conversa.Conversa;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConsultarConversasUseCase {

    private final ConversaRepositorio conversas;
    private final DestinatarioRepositorio destinatarios;

    public ConsultarConversasUseCase(ConversaRepositorio conversas, DestinatarioRepositorio destinatarios) {
        this.conversas = conversas;
        this.destinatarios = destinatarios;
    }

    /** Conversas do cliente, da mais recente para a mais antiga, filtradas pelo nome do destinatário. */
    public List<ResumoConversa> listar(UUID clienteId, String busca) {
        return listar(clienteId, busca, null);
    }

    /** Com {@code status} informado, só as conversas cujo atendimento está nesse status. */
    public List<ResumoConversa> listar(UUID clienteId, String busca, StatusAtendimento status) {
        List<Conversa> doCliente = conversas.listarPorCliente(clienteId).stream()
                .filter(c -> status == null || c.statusAtendimento() == status)
                .toList();
        Map<UUID, Destinatario> destinatariosPorId = destinatarios
                .buscarPorIds(doCliente.stream().map(Conversa::destinatarioId).toList()).stream()
                .collect(Collectors.toMap(Destinatario::id, Function.identity()));
        String termo = busca == null ? "" : busca.strip().toLowerCase(Locale.ROOT);
        return doCliente.stream()
                .map(conversa -> new ResumoConversa(conversa, destinatariosPorId.get(conversa.destinatarioId())))
                .filter(resumo -> resumo.destinatario().nome().toLowerCase(Locale.ROOT).contains(termo))
                .toList();
    }

    public ResumoConversa buscar(UUID clienteId, UUID conversaId) {
        Conversa conversa = conversas.buscarPorId(conversaId)
                .filter(c -> c.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa"));
        Destinatario destinatario = destinatarios.buscarPorId(conversa.destinatarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Destinatário"));
        return new ResumoConversa(conversa, destinatario);
    }
}

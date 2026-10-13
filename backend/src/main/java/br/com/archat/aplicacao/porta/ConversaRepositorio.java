package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.conversa.Conversa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversaRepositorio {

    void salvar(Conversa conversa);

    Optional<Conversa> buscarPorId(UUID id);

    Optional<Conversa> buscarPorDestinatario(UUID destinatarioId);

    /** Conversas do cliente, da mais recente para a mais antiga. */
    List<Conversa> listarPorCliente(UUID clienteId);
}

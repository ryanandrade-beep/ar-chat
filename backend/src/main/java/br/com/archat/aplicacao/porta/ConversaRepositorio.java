package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.conversa.Conversa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversaRepositorio {

    void salvar(Conversa conversa);

    Optional<Conversa> buscarPorId(UUID id);

    Optional<Conversa> buscarPorDestinatario(UUID destinatarioId);

    List<Conversa> listarPorCliente(UUID clienteId);
}

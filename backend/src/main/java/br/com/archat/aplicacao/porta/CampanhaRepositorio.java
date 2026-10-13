package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.campanha.Campanha;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampanhaRepositorio {

    void salvar(Campanha campanha);

    Optional<Campanha> buscarPorId(UUID id);

    /** Campanhas do cliente, da mais recente para a mais antiga. */
    List<Campanha> listarPorCliente(UUID clienteId);

    /** Campanhas criadas no período [de, ate), da mais recente para a mais antiga; {@code clienteId} nulo = todas. */
    List<Campanha> listarPorPeriodo(UUID clienteId, Instant de, Instant ate);
}

package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.campanha.Campanha;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampanhaRepositorio {

    void salvar(Campanha campanha);

    Optional<Campanha> buscarPorId(UUID id);

    List<Campanha> listarPorCliente(UUID clienteId);

    List<Campanha> listarPorPeriodo(UUID clienteId, Instant de, Instant ate);
}

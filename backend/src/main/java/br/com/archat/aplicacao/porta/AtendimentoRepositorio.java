package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.atendimento.Atendimento;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtendimentoRepositorio {

    void salvar(Atendimento atendimento);

    /** O atendimento ainda não finalizado da conversa, se houver (só existe um por vez). */
    Optional<Atendimento> buscarAbertoDaConversa(UUID conversaId);

    /** Atendimentos finalizados no período [de, ate), do mais recente para o mais antigo; clienteId nulo = todos. */
    List<Atendimento> listarFinalizados(UUID clienteId, Instant de, Instant ate);
}

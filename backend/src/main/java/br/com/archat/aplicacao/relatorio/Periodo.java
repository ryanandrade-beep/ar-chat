package br.com.archat.aplicacao.relatorio;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.time.Instant;

/** Intervalo [de, ate) de um relatório. */
public record Periodo(Instant de, Instant ate) {

    public Periodo {
        if (de == null || ate == null) {
            throw new RegraDeNegocioException("Informe a data inicial e a data final do período");
        }
        if (ate.isBefore(de)) {
            throw new RegraDeNegocioException("A data final não pode ser anterior à data inicial");
        }
    }
}

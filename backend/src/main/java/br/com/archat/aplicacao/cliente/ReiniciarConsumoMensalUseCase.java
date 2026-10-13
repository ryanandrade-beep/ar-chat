package br.com.archat.aplicacao.cliente;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;

/** Executado no início de cada mês: o consumo dos clientes pós-pagos volta a zero. */
public class ReiniciarConsumoMensalUseCase {

    private final ClienteRepositorio clientes;
    private final UnidadeDeTrabalho unidadeDeTrabalho;

    public ReiniciarConsumoMensalUseCase(ClienteRepositorio clientes, UnidadeDeTrabalho unidadeDeTrabalho) {
        this.clientes = clientes;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
    }

    public int executar() {
        return unidadeDeTrabalho.executar(clientes::zerarConsumoMensalDosPosPagos);
    }
}

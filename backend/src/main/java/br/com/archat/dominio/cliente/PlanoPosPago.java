package br.com.archat.dominio.cliente;

import br.com.archat.dominio.excecao.SaldoInsuficienteException;

import java.math.BigDecimal;

final class PlanoPosPago implements PlanoPagamento {

    @Override
    public BigDecimal disponivel(Cliente cliente) {
        return cliente.limiteMensal().subtract(cliente.consumoMes());
    }

    @Override
    public void cobrar(Cliente cliente, BigDecimal valor) {
        BigDecimal disponivel = disponivel(cliente);
        if (disponivel.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Limite mensal excedido", disponivel, valor);
        }
        cliente.alterarConsumoMes(cliente.consumoMes().add(valor));
    }

    @Override
    public void estornar(Cliente cliente, BigDecimal valor) {
        cliente.alterarConsumoMes(cliente.consumoMes().subtract(valor));
    }
}

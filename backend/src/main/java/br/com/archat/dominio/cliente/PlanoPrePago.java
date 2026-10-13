package br.com.archat.dominio.cliente;

import br.com.archat.dominio.excecao.SaldoInsuficienteException;

import java.math.BigDecimal;

final class PlanoPrePago implements PlanoPagamento {

    @Override
    public BigDecimal disponivel(Cliente cliente) {
        return cliente.saldo();
    }

    @Override
    public void cobrar(Cliente cliente, BigDecimal valor) {
        if (cliente.saldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente", cliente.saldo(), valor);
        }
        cliente.alterarSaldo(cliente.saldo().subtract(valor));
    }

    @Override
    public void estornar(Cliente cliente, BigDecimal valor) {
        cliente.alterarSaldo(cliente.saldo().add(valor));
    }
}

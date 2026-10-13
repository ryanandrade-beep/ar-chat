package br.com.archat.dominio.cliente;

import java.math.BigDecimal;

/**
 * Estratégia de cobrança de um plano. Cada implementação decide de onde sai o valor
 * de uma mensagem: do saldo (pré-pago) ou do limite mensal (pós-pago).
 */
interface PlanoPagamento {

    BigDecimal disponivel(Cliente cliente);

    void cobrar(Cliente cliente, BigDecimal valor);

    void estornar(Cliente cliente, BigDecimal valor);
}

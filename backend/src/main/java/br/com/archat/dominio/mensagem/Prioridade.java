package br.com.archat.dominio.mensagem;

import java.math.BigDecimal;

public enum Prioridade {
    NORMAL(new BigDecimal("0.25")),
    URGENTE(new BigDecimal("0.50"));

    private final BigDecimal custo;

    Prioridade(BigDecimal custo) {
        this.custo = custo;
    }

    public BigDecimal custo() {
        return custo;
    }
}

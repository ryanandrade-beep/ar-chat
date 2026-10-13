package br.com.archat.dominio.cliente;

public enum TipoPlano {
    PRE_PAGO(new PlanoPrePago()),
    POS_PAGO(new PlanoPosPago());

    private final PlanoPagamento plano;

    TipoPlano(PlanoPagamento plano) {
        this.plano = plano;
    }

    PlanoPagamento plano() {
        return plano;
    }
}

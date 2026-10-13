package br.com.archat.dominio.cliente;

public enum TipoDocumento {
    CPF(11),
    CNPJ(14);

    private final int quantidadeDigitos;

    TipoDocumento(int quantidadeDigitos) {
        this.quantidadeDigitos = quantidadeDigitos;
    }

    public int quantidadeDigitos() {
        return quantidadeDigitos;
    }
}

package br.com.archat.dominio.mensagem;

/** Tipos de mídia que uma mensagem pode levar além do texto. */
public enum TipoMidia {
    IMAGEM(true),
    DOCUMENTO(true),
    AUDIO(false),
    VIDEO(true);

    private final boolean aceitaLegenda;

    TipoMidia(boolean aceitaLegenda) {
        this.aceitaLegenda = aceitaLegenda;
    }

    public boolean aceitaLegenda() {
        return aceitaLegenda;
    }
}

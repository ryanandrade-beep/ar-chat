package br.com.archat.dominio.atendimento;

/** Quem iniciou o atendimento. */
public enum OrigemAtendimento {
    /** O cliente final mandou a primeira mensagem. */
    CLIENTE_FINAL,
    /** A empresa iniciou a conversa. */
    EMPRESA
}

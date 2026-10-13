package br.com.archat.dominio.atendimento;

import java.util.Set;

/** Estados de um atendimento e as transições permitidas entre eles. */
public enum StatusAtendimento {
    /** O cliente final escreveu e a empresa ainda não respondeu. */
    AGUARDANDO,
    /** A empresa está atendendo. */
    EM_ANDAMENTO,
    /** Atendimento pausado pela empresa (ex.: verificando um pedido). */
    EM_ESPERA,
    /** Atendimento encerrado. Uma nova mensagem do cliente abre outro atendimento. */
    FINALIZADO;

    public boolean podeIrPara(StatusAtendimento destino) {
        return switch (this) {
            case AGUARDANDO -> Set.of(EM_ANDAMENTO, EM_ESPERA, FINALIZADO).contains(destino);
            case EM_ANDAMENTO -> Set.of(AGUARDANDO, EM_ESPERA, FINALIZADO).contains(destino);
            case EM_ESPERA -> Set.of(EM_ANDAMENTO, AGUARDANDO, FINALIZADO).contains(destino);
            case FINALIZADO -> false;
        };
    }

    public boolean aberto() {
        return this != FINALIZADO;
    }
}

package br.com.archat.dominio.atendimento;

import java.util.Set;

/** Estados de um atendimento e as transições permitidas entre eles. */
public enum StatusAtendimento {
    AGUARDANDO,
    EM_ANDAMENTO,
    EM_ESPERA,
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

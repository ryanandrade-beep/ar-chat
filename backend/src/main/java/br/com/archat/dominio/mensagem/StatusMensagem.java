package br.com.archat.dominio.mensagem;

import java.util.Set;

/** Estados do ciclo de vida de uma mensagem e as transições permitidas entre eles. */
public enum StatusMensagem {
    ENFILEIRADA,
    PROCESSANDO,
    ENVIADA,
    ENTREGUE,
    LIDA,
    FALHA;

    public boolean podeIrPara(StatusMensagem destino) {
        return proximos().contains(destino);
    }

    public boolean pendente() {
        return this == ENFILEIRADA || this == PROCESSANDO;
    }

    private Set<StatusMensagem> proximos() {
        return switch (this) {
            case ENFILEIRADA -> Set.of(PROCESSANDO);
            case PROCESSANDO -> Set.of(ENVIADA, ENFILEIRADA, FALHA);
            case ENVIADA -> Set.of(ENTREGUE);
            case ENTREGUE -> Set.of(LIDA);
            case LIDA, FALHA -> Set.of();
        };
    }
}

package br.com.archat.aplicacao.porta;

import java.util.function.Supplier;

/** Executa um bloco de forma atômica: ou tudo é gravado, ou nada é. */
public interface UnidadeDeTrabalho {

    <T> T executar(Supplier<T> bloco);

    default void executar(Runnable bloco) {
        executar(() -> {
            bloco.run();
            return null;
        });
    }
}

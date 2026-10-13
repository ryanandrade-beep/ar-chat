package br.com.archat.aplicacao.porta;

import java.time.Duration;

/** Indicadores do processamento da fila, acumulados desde a inicialização do sistema. */
public interface MetricasFila {

    void registrarEnvio(Duration esperaNaFila);

    void registrarFalhaTemporaria();

    void registrarFalhaDefinitiva();

    Contadores contadores();

    record Contadores(long enviadas, long falhasTemporarias, long falhasDefinitivas, Duration esperaMedia) {
    }
}

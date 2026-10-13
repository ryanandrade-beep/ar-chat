package br.com.archat.infraestrutura.fila;

import br.com.archat.aplicacao.porta.MetricasFila;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Component
class MetricasFilaEmMemoria implements MetricasFila {

    private final AtomicLong enviadas = new AtomicLong();
    private final AtomicLong falhasTemporarias = new AtomicLong();
    private final AtomicLong falhasDefinitivas = new AtomicLong();
    private final AtomicLong esperaTotalMs = new AtomicLong();

    @Override
    public void registrarEnvio(Duration esperaNaFila) {
        enviadas.incrementAndGet();
        esperaTotalMs.addAndGet(Math.max(0, esperaNaFila.toMillis()));
    }

    @Override
    public void registrarFalhaTemporaria() {
        falhasTemporarias.incrementAndGet();
    }

    @Override
    public void registrarFalhaDefinitiva() {
        falhasDefinitivas.incrementAndGet();
    }

    @Override
    public Contadores contadores() {
        long total = enviadas.get();
        Duration esperaMedia = total == 0 ? Duration.ZERO : Duration.ofMillis(esperaTotalMs.get() / total);
        return new Contadores(total, falhasTemporarias.get(), falhasDefinitivas.get(), esperaMedia);
    }
}

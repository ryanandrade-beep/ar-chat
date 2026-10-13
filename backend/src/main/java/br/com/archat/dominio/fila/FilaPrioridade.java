package br.com.archat.dominio.fila;

import br.com.archat.dominio.mensagem.Prioridade;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Fila com dois níveis de prioridade. Urgentes saem antes das normais, mas, após
 * {@code limiteUrgentesSeguidas} urgentes consecutivas, uma normal é atendida, se houver,
 * para que as normais não fiquem esperando para sempre (starvation).
 * Dentro de cada prioridade a ordem é FIFO. Enfileirar e retirar custam O(1).
 * <p>
 * Não é thread-safe: quem a compartilha entre threads deve sincronizar o acesso.
 */
public class FilaPrioridade<T> {

    private final Deque<T> urgentes = new ArrayDeque<>();
    private final Deque<T> normais = new ArrayDeque<>();
    private final int limiteUrgentesSeguidas;
    private int urgentesSeguidas;

    public FilaPrioridade(int limiteUrgentesSeguidas) {
        if (limiteUrgentesSeguidas < 1) {
            throw new IllegalArgumentException("O limite de urgentes seguidas deve ser ao menos 1");
        }
        this.limiteUrgentesSeguidas = limiteUrgentesSeguidas;
    }

    public void enfileirar(T item, Prioridade prioridade) {
        (prioridade == Prioridade.URGENTE ? urgentes : normais).addLast(item);
    }

    public Optional<T> proxima() {
        boolean vezDaNormal = urgentesSeguidas >= limiteUrgentesSeguidas && !normais.isEmpty();
        if (!urgentes.isEmpty() && !vezDaNormal) {
            urgentesSeguidas++;
            return Optional.of(urgentes.pollFirst());
        }
        urgentesSeguidas = 0;
        return Optional.ofNullable(normais.pollFirst());
    }

    public int tamanho(Prioridade prioridade) {
        return (prioridade == Prioridade.URGENTE ? urgentes : normais).size();
    }

    public int tamanho() {
        return urgentes.size() + normais.size();
    }

    public boolean vazia() {
        return tamanho() == 0;
    }
}

package br.com.archat.dominio;

import br.com.archat.dominio.fila.FilaPrioridade;
import br.com.archat.dominio.mensagem.Prioridade;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilaPrioridadeTest {

    @Test
    void filaVaziaNaoDevolveNada() {
        assertThat(new FilaPrioridade<String>(3).proxima()).isEmpty();
    }

    @Test
    void urgentesSaemAntesDasNormais() {
        FilaPrioridade<String> fila = new FilaPrioridade<>(3);
        fila.enfileirar("n1", Prioridade.NORMAL);
        fila.enfileirar("u1", Prioridade.URGENTE);

        assertThat(retirarTudo(fila)).containsExactly("u1", "n1");
    }

    @Test
    void mantemOrdemDeChegadaDentroDeCadaPrioridade() {
        FilaPrioridade<String> fila = new FilaPrioridade<>(10);
        fila.enfileirar("n1", Prioridade.NORMAL);
        fila.enfileirar("u1", Prioridade.URGENTE);
        fila.enfileirar("n2", Prioridade.NORMAL);
        fila.enfileirar("u2", Prioridade.URGENTE);

        assertThat(retirarTudo(fila)).containsExactly("u1", "u2", "n1", "n2");
    }

    @Test
    void aposTresUrgentesSeguidasAtendeUmaNormal() {
        FilaPrioridade<String> fila = new FilaPrioridade<>(3);
        fila.enfileirar("n1", Prioridade.NORMAL);
        fila.enfileirar("n2", Prioridade.NORMAL);
        for (int i = 1; i <= 7; i++) {
            fila.enfileirar("u" + i, Prioridade.URGENTE);
        }

        assertThat(retirarTudo(fila)).containsExactly("u1", "u2", "u3", "n1", "u4", "u5", "u6", "n2", "u7");
    }

    @Test
    void semNormaisContinuaAtendendoUrgentes() {
        FilaPrioridade<String> fila = new FilaPrioridade<>(2);
        for (int i = 1; i <= 5; i++) {
            fila.enfileirar("u" + i, Prioridade.URGENTE);
        }

        assertThat(retirarTudo(fila)).containsExactly("u1", "u2", "u3", "u4", "u5");
    }

    @Test
    void informaTamanhoPorPrioridade() {
        FilaPrioridade<String> fila = new FilaPrioridade<>(3);
        fila.enfileirar("n1", Prioridade.NORMAL);
        fila.enfileirar("u1", Prioridade.URGENTE);
        fila.enfileirar("u2", Prioridade.URGENTE);

        assertThat(fila.tamanho(Prioridade.URGENTE)).isEqualTo(2);
        assertThat(fila.tamanho(Prioridade.NORMAL)).isEqualTo(1);
        assertThat(fila.tamanho()).isEqualTo(3);
    }

    @Test
    void recusaLimiteInvalido() {
        assertThatThrownBy(() -> new FilaPrioridade<String>(0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static List<String> retirarTudo(FilaPrioridade<String> fila) {
        List<String> ordem = new ArrayList<>();
        fila.proxima().ifPresent(ordem::add);
        while (!fila.vazia()) {
            fila.proxima().ifPresent(ordem::add);
        }
        return ordem;
    }
}

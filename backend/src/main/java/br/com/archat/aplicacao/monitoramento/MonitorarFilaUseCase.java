package br.com.archat.aplicacao.monitoramento;

import br.com.archat.aplicacao.porta.FilaMensagens;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.MetricasFila;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/** Monitoramento: situação atual da fila e indicadores de processamento desde a inicialização. */
public class MonitorarFilaUseCase {

    private final FilaMensagens fila;
    private final MetricasFila metricas;
    private final MensagemRepositorio mensagens;
    private final Clock relogio;

    public MonitorarFilaUseCase(FilaMensagens fila, MetricasFila metricas, MensagemRepositorio mensagens,
                                Clock relogio) {
        this.fila = fila;
        this.metricas = metricas;
        this.mensagens = mensagens;
        this.relogio = relogio;
    }

    public SituacaoFila emitir() {
        return new SituacaoFila(relogio.instant(), fila.tamanho(Prioridade.URGENTE), fila.tamanho(Prioridade.NORMAL),
                metricas.contadores(), mensagens.contarPorStatus());
    }

    public record SituacaoFila(Instant geradoEm, int pendentesUrgentes, int pendentesNormais,
                                MetricasFila.Contadores processamento, Map<StatusMensagem, Long> mensagensPorStatus) {
    }
}

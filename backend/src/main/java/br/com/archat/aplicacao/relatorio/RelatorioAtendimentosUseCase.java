package br.com.archat.aplicacao.relatorio;

import br.com.archat.aplicacao.porta.AtendimentoRepositorio;
import br.com.archat.aplicacao.porta.ConversaRepositorio;
import br.com.archat.aplicacao.porta.DestinatarioRepositorio;
import br.com.archat.dominio.atendimento.Atendimento;
import br.com.archat.dominio.atendimento.OrigemAtendimento;
import br.com.archat.dominio.conversa.Conversa;
import br.com.archat.dominio.conversa.Destinatario;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Atendimentos finalizados no período: quem foi atendido, quanto tempo a empresa levou para responder
 * pela primeira vez e quanto durou cada atendimento, com médias e a quantidade por dia.
 */
public class RelatorioAtendimentosUseCase {

    private final AtendimentoRepositorio atendimentos;
    private final ConversaRepositorio conversas;
    private final DestinatarioRepositorio destinatarios;
    private final ZoneId fuso;

    public RelatorioAtendimentosUseCase(AtendimentoRepositorio atendimentos, ConversaRepositorio conversas,
                                        DestinatarioRepositorio destinatarios, ZoneId fuso) {
        this.atendimentos = atendimentos;
        this.conversas = conversas;
        this.destinatarios = destinatarios;
        this.fuso = fuso;
    }

    /** Com {@code clienteId} nulo, considera os atendimentos de todos os clientes. */
    public RelatorioAtendimentos emitir(UUID clienteId, Periodo periodo) {
        List<Atendimento> finalizados = atendimentos.listarFinalizados(clienteId, periodo.de(), periodo.ate());
        Map<UUID, Destinatario> destinatarioPorConversa = destinatariosDasConversas(finalizados);

        List<AtendimentoFinalizado> linhas = finalizados.stream()
                .map(a -> new AtendimentoFinalizado(a, Optional.ofNullable(destinatarioPorConversa.get(a.conversaId()))
                        .map(Destinatario::nome).orElse("(contato excluído)")))
                .toList();
        Map<LocalDate, Long> porDia = finalizados.stream().collect(Collectors.groupingBy(
                a -> LocalDate.ofInstant(a.finalizadoEm(), fuso), TreeMap::new, Collectors.counting()));
        long iniciadosPeloCliente = finalizados.stream()
                .filter(a -> a.origem() == OrigemAtendimento.CLIENTE_FINAL).count();

        return new RelatorioAtendimentos(periodo, linhas, finalizados.size(), iniciadosPeloCliente,
                media(finalizados.stream().map(Atendimento::tempoAtePrimeiraResposta)),
                media(finalizados.stream().map(Atendimento::duracao)),
                finalizados.stream().mapToInt(a -> a.mensagensEnviadas() + a.mensagensRecebidas()).average().orElse(0),
                porDia);
    }

    private Map<UUID, Destinatario> destinatariosDasConversas(List<Atendimento> lista) {
        Map<UUID, Conversa> conversasPorId = lista.stream().map(Atendimento::conversaId).distinct()
                .map(conversas::buscarPorId).flatMap(Optional::stream)
                .collect(Collectors.toMap(Conversa::id, Function.identity()));
        Map<UUID, Destinatario> porId = destinatarios.buscarPorIds(conversasPorId.values().stream()
                        .map(Conversa::destinatarioId).toList()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Destinatario::id, Function.identity()));
        return conversasPorId.values().stream().filter(c -> porId.containsKey(c.destinatarioId()))
                .collect(Collectors.toMap(Conversa::id, c -> porId.get(c.destinatarioId())));
    }

    private static Duration media(java.util.stream.Stream<Optional<Duration>> duracoes) {
        List<Duration> validas = duracoes.flatMap(Optional::stream).toList();
        return validas.isEmpty() ? null
                : Duration.ofSeconds(Math.round(validas.stream().mapToLong(Duration::toSeconds).average().orElse(0)));
    }

    public record AtendimentoFinalizado(Atendimento atendimento, String destinatario) {
    }

    /**
     * {@code tempoMedioPrimeiraResposta} considera só os atendimentos iniciados pelo cliente final;
     * fica nulo quando não há nenhum. {@code mediaMensagens} soma enviadas e recebidas.
     */
    public record RelatorioAtendimentos(Periodo periodo, List<AtendimentoFinalizado> atendimentos, long total,
                                        long iniciadosPeloCliente, Duration tempoMedioPrimeiraResposta,
                                        Duration duracaoMedia, double mediaMensagens, Map<LocalDate, Long> porDia) {
    }
}

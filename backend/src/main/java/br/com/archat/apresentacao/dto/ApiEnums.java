package br.com.archat.apresentacao.dto;

import br.com.archat.dominio.atendimento.StatusAtendimento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.financeiro.TipoTransacao;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tradução entre os enums do domínio (em português) e os valores do contrato da API
 * (em inglês, padrão comum em APIs REST).
 */
public final class ApiEnums {

    private static final Map<TipoPlano, String> PLANOS = Map.of(TipoPlano.PRE_PAGO, "prepaid", TipoPlano.POS_PAGO, "postpaid");
    private static final Map<Prioridade, String> PRIORIDADES = Map.of(Prioridade.NORMAL, "normal", Prioridade.URGENTE, "urgent");
    private static final Map<StatusMensagem, String> STATUS = Map.of(
            StatusMensagem.ENFILEIRADA, "queued", StatusMensagem.PROCESSANDO, "processing",
            StatusMensagem.ENVIADA, "sent", StatusMensagem.ENTREGUE, "delivered",
            StatusMensagem.LIDA, "read", StatusMensagem.FALHA, "failed");
    private static final Map<TipoTransacao, String> TRANSACOES = Map.of(
            TipoTransacao.CREDITO, "credit", TipoTransacao.DEBITO, "debit",
            TipoTransacao.ESTORNO, "refund", TipoTransacao.AJUSTE_LIMITE, "limit_adjustment");

    private static final Map<StatusAtendimento, String> ATENDIMENTOS = Map.of(
            StatusAtendimento.AGUARDANDO, "waiting", StatusAtendimento.EM_ANDAMENTO, "in_progress",
            StatusAtendimento.EM_ESPERA, "on_hold", StatusAtendimento.FINALIZADO, "finished");

    private ApiEnums() {
    }

    public static String plano(TipoPlano plano) { return PLANOS.get(plano); }
    public static String prioridade(Prioridade prioridade) { return PRIORIDADES.get(prioridade); }
    public static String status(StatusMensagem status) { return STATUS.get(status); }
    public static String transacao(TipoTransacao tipo) { return TRANSACOES.get(tipo); }
    public static String atendimento(StatusAtendimento status) { return ATENDIMENTOS.get(status); }
    public static StatusAtendimento atendimento(String valor) { return ler(ATENDIMENTOS, valor, "status"); }

    /** Só o WhatsApp Web está disponível; o WhatsApp Business (API oficial) ainda não. */
    public static void exigirCanalDisponivel(String canal) {
        if (canal == null || canal.isBlank() || canal.equalsIgnoreCase("whatsapp_web")) {
            return;
        }
        if (canal.equalsIgnoreCase("whatsapp_business")) {
            throw new RegraDeNegocioException("O canal WhatsApp Business estará disponível em breve; use whatsapp_web");
        }
        throw new RegraDeNegocioException("channel inválido: use whatsapp_web");
    }

    public static TipoPlano plano(String valor) { return ler(PLANOS, valor, "planType"); }
    public static Prioridade prioridade(String valor) { return ler(PRIORIDADES, valor, "priority"); }
    public static StatusMensagem status(String valor) { return ler(STATUS, valor, "status"); }

    public static TipoDocumento documento(String valor) {
        try {
            return TipoDocumento.valueOf(valor == null ? "" : valor.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("documentType inválido: use CPF ou CNPJ");
        }
    }

    private static <E> E ler(Map<E, String> mapa, String valor, String campo) {
        Map<String, E> inverso = mapa.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        E encontrado = valor == null ? null : inverso.get(valor.strip().toLowerCase());
        if (encontrado == null) {
            throw new RegraDeNegocioException("%s inválido: use %s".formatted(campo,
                    String.join(", ", mapa.values().stream().sorted().map(Function.identity()).toList())));
        }
        return encontrado;
    }
}

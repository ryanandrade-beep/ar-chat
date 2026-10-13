package br.com.archat.dominio.atendimento;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Um ciclo de atendimento de uma conversa: abre quando o cliente final escreve (ou a empresa inicia),
 * muda de status conforme as mensagens e as ações da empresa, e termina ao ser finalizado.
 * Uma conversa tem vários atendimentos ao longo do tempo, mas só um aberto por vez.
 */
public class Atendimento {

    private final UUID id;
    private final UUID conversaId;
    private final UUID clienteId;
    private final OrigemAtendimento origem;
    private StatusAtendimento status;
    private final Instant abertoEm;
    private Instant primeiraRespostaEm;
    private Instant finalizadoEm;
    private int mensagensEnviadas;
    private int mensagensRecebidas;

    private Atendimento(UUID id, UUID conversaId, UUID clienteId, OrigemAtendimento origem, StatusAtendimento status,
                        Instant abertoEm, Instant primeiraRespostaEm, Instant finalizadoEm, int mensagensEnviadas,
                        int mensagensRecebidas) {
        this.id = Objects.requireNonNull(id);
        this.conversaId = Objects.requireNonNull(conversaId);
        this.clienteId = Objects.requireNonNull(clienteId);
        this.origem = Objects.requireNonNull(origem);
        this.status = Objects.requireNonNull(status);
        this.abertoEm = Objects.requireNonNull(abertoEm);
        this.primeiraRespostaEm = primeiraRespostaEm;
        this.finalizadoEm = finalizadoEm;
        this.mensagensEnviadas = mensagensEnviadas;
        this.mensagensRecebidas = mensagensRecebidas;
    }

    /** O cliente final escreveu: o atendimento nasce aguardando a resposta da empresa. */
    public static Atendimento abertoPeloClienteFinal(UUID conversaId, UUID clienteId, Instant agora) {
        return new Atendimento(UUID.randomUUID(), conversaId, clienteId, OrigemAtendimento.CLIENTE_FINAL,
                StatusAtendimento.AGUARDANDO, agora, null, null, 0, 1);
    }

    /** A empresa iniciou a conversa: o atendimento nasce em andamento. */
    public static Atendimento abertoPelaEmpresa(UUID conversaId, UUID clienteId, Instant agora) {
        return new Atendimento(UUID.randomUUID(), conversaId, clienteId, OrigemAtendimento.EMPRESA,
                StatusAtendimento.EM_ANDAMENTO, agora, null, null, 1, 0);
    }

    public static Atendimento reconstituir(UUID id, UUID conversaId, UUID clienteId, OrigemAtendimento origem,
                                           StatusAtendimento status, Instant abertoEm, Instant primeiraRespostaEm,
                                           Instant finalizadoEm, int mensagensEnviadas, int mensagensRecebidas) {
        return new Atendimento(id, conversaId, clienteId, origem, status, abertoEm, primeiraRespostaEm, finalizadoEm,
                mensagensEnviadas, mensagensRecebidas);
    }

    /** A empresa enviou uma mensagem: o atendimento passa (ou volta) a estar em andamento. */
    public void registrarEnvio(Instant agora) {
        exigirAberto();
        mensagensEnviadas++;
        if (origem == OrigemAtendimento.CLIENTE_FINAL && primeiraRespostaEm == null) {
            primeiraRespostaEm = agora;
        }
        if (status != StatusAtendimento.EM_ANDAMENTO) {
            status = StatusAtendimento.EM_ANDAMENTO;
        }
    }

    /** O cliente final escreveu: agora é a vez da empresa responder. */
    public void registrarRecebimento() {
        exigirAberto();
        mensagensRecebidas++;
        status = StatusAtendimento.AGUARDANDO;
    }

    public void colocarEmEspera() {
        mudarPara(StatusAtendimento.EM_ESPERA);
    }

    public void retomar() {
        if (status != StatusAtendimento.EM_ESPERA) {
            throw new RegraDeNegocioException("Só um atendimento em espera pode ser retomado");
        }
        mudarPara(StatusAtendimento.EM_ANDAMENTO);
    }

    public void finalizar(Instant agora) {
        mudarPara(StatusAtendimento.FINALIZADO);
        finalizadoEm = agora;
    }

    /** Tempo entre a primeira mensagem do cliente final e a primeira resposta da empresa. */
    public Optional<Duration> tempoAtePrimeiraResposta() {
        return Optional.ofNullable(primeiraRespostaEm).map(resposta -> Duration.between(abertoEm, resposta));
    }

    public Optional<Duration> duracao() {
        return Optional.ofNullable(finalizadoEm).map(fim -> Duration.between(abertoEm, fim));
    }

    private void mudarPara(StatusAtendimento destino) {
        if (!status.podeIrPara(destino)) {
            throw new RegraDeNegocioException("Atendimento não pode passar de %s para %s".formatted(status, destino));
        }
        status = destino;
    }

    private void exigirAberto() {
        if (!status.aberto()) {
            throw new RegraDeNegocioException("Atendimento já finalizado");
        }
    }

    public UUID id() { return id; }
    public UUID conversaId() { return conversaId; }
    public UUID clienteId() { return clienteId; }
    public OrigemAtendimento origem() { return origem; }
    public StatusAtendimento status() { return status; }
    public Instant abertoEm() { return abertoEm; }
    public Instant primeiraRespostaEm() { return primeiraRespostaEm; }
    public Instant finalizadoEm() { return finalizadoEm; }
    public int mensagensEnviadas() { return mensagensEnviadas; }
    public int mensagensRecebidas() { return mensagensRecebidas; }
}

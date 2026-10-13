package br.com.archat.dominio.conversa;

import br.com.archat.dominio.atendimento.StatusAtendimento;
import br.com.archat.dominio.mensagem.Mensagem;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Conversa {

    private static final int TAMANHO_RESUMO = 200;

    private final UUID id;
    private final UUID clienteId;
    private final UUID destinatarioId;
    private String ultimaMensagem;
    private Instant dataUltimaMensagem;
    private int naoLidas;
    /** Status do atendimento atual; FINALIZADO quando não há atendimento aberto. */
    private StatusAtendimento statusAtendimento;

    private Conversa(UUID id, UUID clienteId, UUID destinatarioId, String ultimaMensagem,
                     Instant dataUltimaMensagem, int naoLidas, StatusAtendimento statusAtendimento) {
        this.id = Objects.requireNonNull(id);
        this.clienteId = Objects.requireNonNull(clienteId);
        this.destinatarioId = Objects.requireNonNull(destinatarioId);
        this.ultimaMensagem = ultimaMensagem;
        this.dataUltimaMensagem = dataUltimaMensagem;
        this.naoLidas = naoLidas;
        this.statusAtendimento = statusAtendimento == null ? StatusAtendimento.FINALIZADO : statusAtendimento;
    }

    public static Conversa iniciar(UUID clienteId, UUID destinatarioId) {
        return new Conversa(UUID.randomUUID(), clienteId, destinatarioId, null, null, 0, StatusAtendimento.FINALIZADO);
    }

    public static Conversa reconstituir(UUID id, UUID clienteId, UUID destinatarioId, String ultimaMensagem,
                                        Instant dataUltimaMensagem, int naoLidas,
                                        StatusAtendimento statusAtendimento) {
        return new Conversa(id, clienteId, destinatarioId, ultimaMensagem, dataUltimaMensagem, naoLidas,
                statusAtendimento);
    }

    public void registrar(Mensagem mensagem) {
        String conteudo = mensagem.conteudo();
        ultimaMensagem = conteudo.length() > TAMANHO_RESUMO ? conteudo.substring(0, TAMANHO_RESUMO) : conteudo;
        dataUltimaMensagem = mensagem.criadaEm();
    }

    /** Mensagem do cliente final: entra como não lida. */
    public void registrarRecebida(Mensagem mensagem) {
        registrar(mensagem);
        naoLidas++;
    }

    public void marcarComoLida() {
        naoLidas = 0;
    }

    public void atualizarStatusAtendimento(StatusAtendimento status) {
        statusAtendimento = status;
    }

    public boolean pertenceA(UUID clienteId) {
        return this.clienteId.equals(clienteId);
    }

    public UUID id() { return id; }
    public UUID clienteId() { return clienteId; }
    public UUID destinatarioId() { return destinatarioId; }
    public String ultimaMensagem() { return ultimaMensagem; }
    public Instant dataUltimaMensagem() { return dataUltimaMensagem; }
    public int naoLidas() { return naoLidas; }
    public StatusAtendimento statusAtendimento() { return statusAtendimento; }
}

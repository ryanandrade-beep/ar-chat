package br.com.archat.aplicacao.atendimento;

import br.com.archat.aplicacao.porta.ConversaRepositorio;
import br.com.archat.aplicacao.porta.DestinatarioRepositorio;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.conversa.Conversa;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.mensagem.Mensagem;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Registra uma mensagem escrita pelo cliente final: grava na conversa, conta como não lida e abre ou
 * atualiza o atendimento (status "aguardando"). Não tem custo e não passa pela fila.
 */
public class ReceberMensagemUseCase {

    private final DestinatarioRepositorio destinatarios;
    private final ConversaRepositorio conversas;
    private final MensagemRepositorio mensagens;
    private final RegistroDeAtendimento atendimentos;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public ReceberMensagemUseCase(DestinatarioRepositorio destinatarios, ConversaRepositorio conversas,
                                  MensagemRepositorio mensagens, RegistroDeAtendimento atendimentos,
                                  UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.destinatarios = destinatarios;
        this.conversas = conversas;
        this.mensagens = mensagens;
        this.atendimentos = atendimentos;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    /** Simulação para testes e demonstração: o cliente final "responde" na conversa informada. */
    public Mensagem simularResposta(UUID clienteId, UUID conversaId, String conteudo) {
        return unidadeDeTrabalho.executar(() -> {
            Conversa conversa = conversas.buscarPorId(conversaId)
                    .filter(c -> c.pertenceA(clienteId))
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa"));
            return registrar(conversa, conteudo, null, relogio.instant());
        });
    }

    /**
     * Mensagem que chegou pelo canal (webhook). O número é procurado entre os destinatários cadastrados;
     * se mais de um cliente tiver o mesmo contato, vai para a conversa com atividade mais recente.
     * Mensagens repetidas (mesmo idExterno) e de números não cadastrados são ignoradas.
     */
    public Optional<Mensagem> doCanal(String telefoneOuGrupo, String conteudo, String idExterno, Instant quando) {
        if (idExterno != null && mensagens.buscarPorIdExterno(idExterno).isPresent()) {
            return Optional.empty();
        }
        return unidadeDeTrabalho.executar(() -> conversaDoRemetente(telefoneOuGrupo)
                .map(conversa -> registrar(conversa, conteudo, idExterno, quando == null ? relogio.instant() : quando)));
    }

    private Optional<Conversa> conversaDoRemetente(String telefoneOuGrupo) {
        List<Destinatario> candidatos = destinatarios.buscarPorTelefones(variacoes(telefoneOuGrupo));
        return candidatos.stream()
                .map(d -> conversas.buscarPorDestinatario(d.id()).orElseGet(() -> Conversa.iniciar(d.clienteId(), d.id())))
                .max(Comparator.comparing(c -> c.dataUltimaMensagem() == null ? Instant.EPOCH : c.dataUltimaMensagem()));
    }

    /** O mesmo contato pode estar cadastrado com ou sem o DDI 55. */
    private static Set<String> variacoes(String telefoneOuGrupo) {
        String valor = telefoneOuGrupo == null ? "" : telefoneOuGrupo.strip().toLowerCase();
        Set<String> variacoes = new LinkedHashSet<>();
        variacoes.add(valor);
        if (!valor.contains("-") && valor.startsWith("55") && valor.length() > 11) {
            variacoes.add(valor.substring(2));
        }
        return variacoes;
    }

    private Mensagem registrar(Conversa conversa, String conteudo, String idExterno, Instant quando) {
        Mensagem mensagem = Mensagem.recebidaDoDestinatario(conversa.id(), conteudo, idExterno, quando);
        conversa.registrarRecebida(mensagem);
        conversas.salvar(conversa);
        mensagens.salvar(mensagem);
        atendimentos.aoReceber(conversa, quando);
        conversas.salvar(conversa);
        return mensagem;
    }
}

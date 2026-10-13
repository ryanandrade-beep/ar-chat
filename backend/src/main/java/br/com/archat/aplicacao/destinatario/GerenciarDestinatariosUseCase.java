package br.com.archat.aplicacao.destinatario;

import br.com.archat.aplicacao.porta.DestinatarioRepositorio;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.conversa.Destinatario;
import br.com.archat.dominio.conversa.TipoDestinatario;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.List;
import java.util.UUID;

/** Cadastro dos contatos de um cliente. Cada cliente só enxerga e altera os próprios contatos. */
public class GerenciarDestinatariosUseCase {

    private final DestinatarioRepositorio destinatarios;
    private final MensagemRepositorio mensagens;
    private final UnidadeDeTrabalho unidadeDeTrabalho;

    public GerenciarDestinatariosUseCase(DestinatarioRepositorio destinatarios, MensagemRepositorio mensagens,
                                         UnidadeDeTrabalho unidadeDeTrabalho) {
        this.destinatarios = destinatarios;
        this.mensagens = mensagens;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
    }

    public List<Destinatario> listar(UUID clienteId) {
        return destinatarios.listarPorCliente(clienteId);
    }

    public Destinatario buscar(UUID clienteId, UUID destinatarioId) {
        return doCliente(clienteId, destinatarioId);
    }

    /** {@code telefone} é o número do contato ou, para grupos, o ID do grupo no WhatsApp. */
    public Destinatario cadastrar(UUID clienteId, TipoDestinatario tipo, String nome, String telefone,
                                  boolean aceitaOfertas) {
        Destinatario destinatario = Destinatario.novo(clienteId, tipo, nome, telefone, aceitaOfertas);
        return unidadeDeTrabalho.executar(() -> {
            garantirTelefoneLivre(destinatario);
            destinatarios.salvar(destinatario);
            return destinatario;
        });
    }

    public Destinatario alterar(UUID clienteId, UUID destinatarioId, TipoDestinatario tipo, String nome,
                                String telefone, boolean aceitaOfertas) {
        return unidadeDeTrabalho.executar(() -> {
            Destinatario alterado = doCliente(clienteId, destinatarioId).alterar(tipo, nome, telefone, aceitaOfertas);
            garantirTelefoneLivre(alterado);
            destinatarios.salvar(alterado);
            return alterado;
        });
    }

    public void excluir(UUID clienteId, UUID destinatarioId) {
        unidadeDeTrabalho.executar(() -> {
            Destinatario destinatario = doCliente(clienteId, destinatarioId);
            if (mensagens.existeParaDestinatario(destinatario.id())) {
                throw new RegraDeNegocioException("Destinatário com mensagens não pode ser excluído");
            }
            destinatarios.excluir(destinatario.id());
        });
    }

    private Destinatario doCliente(UUID clienteId, UUID destinatarioId) {
        return destinatarios.buscarPorId(destinatarioId)
                .filter(d -> d.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Destinatário"));
    }

    private void garantirTelefoneLivre(Destinatario destinatario) {
        destinatarios.buscarPorClienteETelefone(destinatario.clienteId(), destinatario.telefone())
                .filter(existente -> !existente.id().equals(destinatario.id()))
                .ifPresent(existente -> {
                    throw new RegraDeNegocioException(destinatario.grupo()
                            ? "Este grupo já está cadastrado" : "Já existe um contato com este telefone");
                });
    }
}

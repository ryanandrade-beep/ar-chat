package br.com.archat.aplicacao.mensagem;

import br.com.archat.aplicacao.porta.ConversaRepositorio;
import br.com.archat.aplicacao.porta.FiltroMensagens;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.mensagem.Mensagem;

import java.util.List;
import java.util.UUID;

public class ConsultarMensagensUseCase {

    private final MensagemRepositorio mensagens;
    private final ConversaRepositorio conversas;

    public ConsultarMensagensUseCase(MensagemRepositorio mensagens, ConversaRepositorio conversas) {
        this.mensagens = mensagens;
        this.conversas = conversas;
    }

    public List<Mensagem> listar(FiltroMensagens filtro) {
        if (filtro.conversaId() != null) {
            garantirQueConversaPertence(filtro.clienteId(), filtro.conversaId());
        }
        return mensagens.listar(filtro);
    }

    public Mensagem buscar(UUID clienteId, UUID mensagemId) {
        return mensagens.buscarDoCliente(mensagemId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mensagem"));
    }

    private void garantirQueConversaPertence(UUID clienteId, UUID conversaId) {
        conversas.buscarPorId(conversaId)
                .filter(c -> c.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa"));
    }
}

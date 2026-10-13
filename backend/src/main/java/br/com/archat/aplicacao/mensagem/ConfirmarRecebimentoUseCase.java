package br.com.archat.aplicacao.mensagem;

import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.mensagem.Mensagem;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.time.Clock;
import java.util.UUID;

/**
 * Confirmações vindas do destinatário. Como o canal de envio é simulado, a entrega é confirmada
 * pelo próprio worker e a leitura por uma rota de simulação.
 */
public class ConfirmarRecebimentoUseCase {

    private final MensagemRepositorio mensagens;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public ConfirmarRecebimentoUseCase(MensagemRepositorio mensagens, UnidadeDeTrabalho unidadeDeTrabalho,
                                       Clock relogio) {
        this.mensagens = mensagens;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public void confirmarEntrega(UUID mensagemId) {
        unidadeDeTrabalho.executar(() -> mensagens.buscarPorId(mensagemId)
                .filter(m -> m.status() == StatusMensagem.ENVIADA)
                .ifPresent(m -> {
                    m.confirmarEntrega(relogio.instant());
                    mensagens.salvar(m);
                }));
    }

    public Mensagem confirmarLeitura(UUID clienteId, UUID mensagemId) {
        return unidadeDeTrabalho.executar(() -> {
            Mensagem mensagem = mensagens.buscarDoCliente(mensagemId, clienteId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Mensagem"));
            mensagem.confirmarLeitura(relogio.instant());
            mensagens.salvar(mensagem);
            return mensagem;
        });
    }
}

package br.com.archat.aplicacao.mensagem;

import br.com.archat.aplicacao.porta.FilaMensagens;
import br.com.archat.aplicacao.porta.MensagemRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.mensagem.Mensagem;

import java.time.Clock;
import java.util.List;

/**
 * A fila vive em memória; ao iniciar o sistema, ela é reconstruída a partir das mensagens
 * que ficaram pendentes no banco, na ordem de chegada.
 */
public class RecuperarFilaUseCase {

    private final MensagemRepositorio mensagens;
    private final FilaMensagens fila;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public RecuperarFilaUseCase(MensagemRepositorio mensagens, FilaMensagens fila,
                                UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.mensagens = mensagens;
        this.fila = fila;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public int executar() {
        List<Mensagem> pendentes = unidadeDeTrabalho.executar(() -> {
            List<Mensagem> lista = mensagens.listarPendentes();
            lista.forEach(m -> {
                m.devolverParaFila(relogio.instant());
                mensagens.salvar(m);
            });
            return lista;
        });
        pendentes.forEach(m -> fila.enfileirar(m.id(), m.prioridade()));
        return pendentes.size();
    }
}

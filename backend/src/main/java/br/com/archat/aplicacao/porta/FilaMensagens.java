package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.mensagem.Prioridade;

import java.util.UUID;

/** Fila de mensagens aguardando envio. Guarda apenas os identificadores. */
public interface FilaMensagens {

    void enfileirar(UUID mensagemId, Prioridade prioridade);

    int tamanho(Prioridade prioridade);

    default int tamanho() {
        return tamanho(Prioridade.URGENTE) + tamanho(Prioridade.NORMAL);
    }
}

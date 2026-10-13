package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.StatusMensagem;

import java.util.UUID;

/** Filtros de consulta de mensagens; campos nulos são ignorados, exceto o cliente. */
public record FiltroMensagens(UUID clienteId, UUID conversaId, StatusMensagem status, Prioridade prioridade) {
}

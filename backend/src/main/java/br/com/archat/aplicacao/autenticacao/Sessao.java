package br.com.archat.aplicacao.autenticacao;

import java.time.Instant;
import java.util.UUID;

/** Quem está fazendo a requisição: um cliente ou um administrador. */
public record Sessao(Perfil perfil, UUID id, Instant expiraEm) {

    public enum Perfil {
        CLIENTE,
        ADMINISTRADOR
    }
}

package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.Objects;
import java.util.UUID;

/** Usuário da área administrativa. A senha só existe aqui na forma de hash. */
public record Administrador(UUID id, String nome, String login, String senhaHash) {

    public Administrador {
        Objects.requireNonNull(id);
        Objects.requireNonNull(senhaHash);
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do administrador é obrigatório");
        }
        if (login == null || !login.strip().matches("[a-zA-Z0-9._-]{3,50}")) {
            throw new RegraDeNegocioException("Login deve ter de 3 a 50 caracteres (letras, números, . _ -)");
        }
        nome = nome.strip();
        login = login.strip().toLowerCase();
    }
}

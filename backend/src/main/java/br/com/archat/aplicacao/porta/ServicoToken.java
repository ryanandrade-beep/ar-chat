package br.com.archat.aplicacao.porta;

import br.com.archat.aplicacao.autenticacao.Sessao;

import java.util.Optional;

public interface ServicoToken {

    String gerar(Sessao sessao);

    Optional<Sessao> validar(String token);
}

package br.com.archat.aplicacao.autenticacao;

import br.com.archat.aplicacao.excecao.NaoAutenticadoException;
import br.com.archat.aplicacao.porta.Administrador;
import br.com.archat.aplicacao.porta.AdministradorRepositorio;
import br.com.archat.aplicacao.porta.CodificadorSenha;
import br.com.archat.aplicacao.porta.ServicoToken;

import java.time.Clock;
import java.time.Duration;

public class AutenticarAdministradorUseCase {

    private final AdministradorRepositorio administradores;
    private final CodificadorSenha codificador;
    private final ServicoToken tokens;
    private final Clock relogio;
    private final Duration validadeToken;

    public AutenticarAdministradorUseCase(AdministradorRepositorio administradores, CodificadorSenha codificador,
                                          ServicoToken tokens, Clock relogio, Duration validadeToken) {
        this.administradores = administradores;
        this.codificador = codificador;
        this.tokens = tokens;
        this.relogio = relogio;
        this.validadeToken = validadeToken;
    }

    public String executar(String login, String senha) {
        Administrador administrador = administradores.buscarPorLogin(login)
                .filter(adm -> codificador.confere(senha, adm.senhaHash()))
                .orElseThrow(() -> new NaoAutenticadoException("Login ou senha inválidos"));
        return tokens.gerar(new Sessao(Sessao.Perfil.ADMINISTRADOR, administrador.id(),
                relogio.instant().plus(validadeToken)));
    }
}

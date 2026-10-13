package br.com.archat.aplicacao.porta;

public interface CodificadorSenha {

    String codificar(String senha);

    boolean confere(String senha, String hash);
}

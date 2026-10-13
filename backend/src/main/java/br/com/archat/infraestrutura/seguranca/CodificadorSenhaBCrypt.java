package br.com.archat.infraestrutura.seguranca;

import br.com.archat.aplicacao.porta.CodificadorSenha;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class CodificadorSenhaBCrypt implements CodificadorSenha {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senha) {
        return bcrypt.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return senha != null && bcrypt.matches(senha, hash);
    }
}

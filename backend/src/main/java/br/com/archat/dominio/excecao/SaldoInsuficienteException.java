package br.com.archat.dominio.excecao;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends DominioException {

    public SaldoInsuficienteException(String mensagem, BigDecimal disponivel, BigDecimal necessario) {
        super("%s: disponível R$ %s, necessário R$ %s".formatted(mensagem, disponivel, necessario));
    }
}

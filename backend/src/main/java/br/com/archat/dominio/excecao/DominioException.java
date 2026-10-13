package br.com.archat.dominio.excecao;

/** Base das exceções que representam a violação de uma regra do domínio. */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}

package br.com.archat.dominio.excecao;

public class RecursoNaoEncontradoException extends DominioException {

    public RecursoNaoEncontradoException(String recurso) {
        super(recurso + " não encontrado(a)");
    }
}

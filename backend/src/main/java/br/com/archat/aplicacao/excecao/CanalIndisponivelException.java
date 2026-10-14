package br.com.archat.aplicacao.excecao;

/** O canal externo (ex.: Z-API) recusou ou não respondeu a uma operação de conexão. */
public class CanalIndisponivelException extends RuntimeException {

    public CanalIndisponivelException(String mensagem) {
        super(mensagem);
    }
}

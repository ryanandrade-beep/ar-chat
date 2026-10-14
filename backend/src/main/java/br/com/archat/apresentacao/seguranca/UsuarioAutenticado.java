package br.com.archat.apresentacao.seguranca;

import br.com.archat.aplicacao.autenticacao.Sessao;
import br.com.archat.aplicacao.excecao.AcessoNegadoException;

import java.util.UUID;

/** Parâmetro de controller que aceita cliente ou administrador. */
public record UsuarioAutenticado(Sessao sessao) {

    public boolean administrador() {
        return sessao.perfil() == Sessao.Perfil.ADMINISTRADOR;
    }

    public void exigirAcessoAoCliente(UUID clienteId) {
        if (!administrador() && !sessao.id().equals(clienteId)) {
            throw new AcessoNegadoException("Acesso permitido apenas aos próprios dados");
        }
    }
}

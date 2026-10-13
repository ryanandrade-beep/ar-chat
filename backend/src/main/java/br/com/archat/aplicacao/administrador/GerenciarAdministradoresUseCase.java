package br.com.archat.aplicacao.administrador;

import br.com.archat.aplicacao.porta.Administrador;
import br.com.archat.aplicacao.porta.AdministradorRepositorio;
import br.com.archat.aplicacao.porta.CodificadorSenha;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.List;
import java.util.UUID;

public class GerenciarAdministradoresUseCase {

    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final AdministradorRepositorio administradores;
    private final CodificadorSenha codificador;
    private final UnidadeDeTrabalho unidadeDeTrabalho;

    public GerenciarAdministradoresUseCase(AdministradorRepositorio administradores, CodificadorSenha codificador,
                                           UnidadeDeTrabalho unidadeDeTrabalho) {
        this.administradores = administradores;
        this.codificador = codificador;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
    }

    public List<Administrador> listar() {
        return administradores.listarTodos();
    }

    public Administrador cadastrar(String nome, String login, String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraDeNegocioException("A senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        Administrador novo = new Administrador(UUID.randomUUID(), nome, login, codificador.codificar(senha));
        return unidadeDeTrabalho.executar(() -> {
            if (administradores.buscarPorLogin(novo.login()).isPresent()) {
                throw new RegraDeNegocioException("Já existe um administrador com este login");
            }
            administradores.salvar(novo);
            return novo;
        });
    }

    public void excluir(UUID solicitanteId, UUID administradorId) {
        if (solicitanteId.equals(administradorId)) {
            throw new RegraDeNegocioException("O administrador não pode excluir a si mesmo");
        }
        unidadeDeTrabalho.executar(() -> {
            administradores.buscarPorId(administradorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador"));
            administradores.excluir(administradorId);
        });
    }

    /** Cria o primeiro administrador quando ainda não há nenhum, para a plataforma poder ser usada. */
    public boolean criarInicialSeNecessario(String nome, String login, String senha) {
        if (administradores.contar() > 0) {
            return false;
        }
        cadastrar(nome, login, senha);
        return true;
    }
}

package br.com.archat.aplicacao.porta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdministradorRepositorio {

    void salvar(Administrador administrador);

    Optional<Administrador> buscarPorId(UUID id);

    Optional<Administrador> buscarPorLogin(String login);

    List<Administrador> listarTodos();

    void excluir(UUID id);

    long contar();
}

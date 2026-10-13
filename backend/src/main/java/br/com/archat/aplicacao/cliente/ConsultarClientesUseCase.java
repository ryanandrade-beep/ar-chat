package br.com.archat.aplicacao.cliente;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;

import java.util.List;
import java.util.UUID;

public class ConsultarClientesUseCase {

    private final ClienteRepositorio clientes;

    public ConsultarClientesUseCase(ClienteRepositorio clientes) {
        this.clientes = clientes;
    }

    public List<Cliente> listar() {
        return clientes.listarTodos();
    }

    public Cliente buscar(UUID clienteId) {
        return clientes.buscarPorId(clienteId).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente"));
    }
}

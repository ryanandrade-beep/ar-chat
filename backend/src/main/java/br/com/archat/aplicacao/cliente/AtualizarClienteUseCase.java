package br.com.archat.aplicacao.cliente;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.excecao.RecursoNaoEncontradoException;

import java.util.UUID;

public class AtualizarClienteUseCase {

    private final ClienteRepositorio clientes;
    private final UnidadeDeTrabalho unidadeDeTrabalho;

    public AtualizarClienteUseCase(ClienteRepositorio clientes, UnidadeDeTrabalho unidadeDeTrabalho) {
        this.clientes = clientes;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
    }

    public Cliente executar(UUID clienteId, String nome, boolean ativo) {
        return unidadeDeTrabalho.executar(() -> {
            Cliente cliente = clientes.buscarPorIdParaAtualizacao(clienteId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente"));
            cliente.renomear(nome);
            if (ativo) {
                cliente.ativar();
            } else {
                cliente.inativar();
            }
            clientes.salvar(cliente);
            return cliente;
        });
    }
}

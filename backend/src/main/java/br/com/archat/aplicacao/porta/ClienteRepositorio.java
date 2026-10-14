package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositorio {

    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorIdParaAtualizacao(UUID id);

    Optional<Cliente> buscarPorDocumento(Documento documento);

    boolean existeComDocumento(Documento documento);

    List<Cliente> listarTodos();

    int zerarConsumoMensalDosPosPagos();
}

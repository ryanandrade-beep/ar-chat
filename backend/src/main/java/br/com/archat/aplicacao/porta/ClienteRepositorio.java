package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositorio {

    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    /** Busca o cliente bloqueando o registro até o fim da transação, para cobranças concorrentes. */
    Optional<Cliente> buscarPorIdParaAtualizacao(UUID id);

    Optional<Cliente> buscarPorDocumento(Documento documento);

    boolean existeComDocumento(Documento documento);

    List<Cliente> listarTodos();

    /** Zera o consumo do mês dos clientes pós-pagos e retorna quantos foram afetados. */
    int zerarConsumoMensalDosPosPagos();
}

package br.com.archat.aplicacao.porta;

import br.com.archat.dominio.cliente.ContaDeAcesso;

import java.util.Optional;
import java.util.UUID;

public interface ContaDeAcessoRepositorio {

    void salvar(ContaDeAcesso conta);

    Optional<ContaDeAcesso> buscarPorEmail(String email);

    Optional<ContaDeAcesso> buscarPorCliente(UUID clienteId);

    boolean existeComEmail(String email);
}

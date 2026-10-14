package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.ContaDeAcessoRepositorio;
import br.com.archat.dominio.cliente.ContaDeAcesso;
import br.com.archat.infraestrutura.persistencia.entidade.ContaDeAcessoEntity;
import br.com.archat.infraestrutura.persistencia.jpa.ContaDeAcessoJpa;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class ContaDeAcessoRepositorioJpa implements ContaDeAcessoRepositorio {

    private final ContaDeAcessoJpa jpa;

    ContaDeAcessoRepositorioJpa(ContaDeAcessoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(ContaDeAcesso c) {
        jpa.save(new ContaDeAcessoEntity(c.clienteId(), c.email(), c.telefone(), c.senhaHash(), c.versaoTermos(),
                c.termosAceitosEm(), c.codigoHash(), c.codigoExpiraEm(), c.tentativasCodigo()));
    }

    @Override
    public Optional<ContaDeAcesso> buscarPorEmail(String email) {
        return jpa.findByEmail(email).map(ContaDeAcessoRepositorioJpa::paraDominio);
    }

    @Override
    public Optional<ContaDeAcesso> buscarPorCliente(UUID clienteId) {
        return jpa.findById(clienteId).map(ContaDeAcessoRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private static ContaDeAcesso paraDominio(ContaDeAcessoEntity e) {
        return ContaDeAcesso.reconstituir(e.getClienteId(), e.getEmail(), e.getTelefone(), e.getSenhaHash(),
                e.getVersaoTermos(), e.getTermosAceitosEm(), e.getCodigoHash(), e.getCodigoExpiraEm(),
                e.getTentativasCodigo());
    }
}

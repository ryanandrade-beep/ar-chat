package br.com.archat.infraestrutura.persistencia;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.infraestrutura.persistencia.entidade.ClienteEntity;
import br.com.archat.infraestrutura.persistencia.jpa.ClienteJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class ClienteRepositorioJpa implements ClienteRepositorio {

    private final ClienteJpa jpa;

    ClienteRepositorioJpa(ClienteJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Cliente c) {
        jpa.save(new ClienteEntity(c.id(), c.nome(), c.documento().numero(), c.documento().tipo(), c.tipoPlano(),
                c.saldo(), c.limiteMensal(), c.consumoMes(), c.ativo(), c.criadoEm()));
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return jpa.findById(id).map(ClienteRepositorioJpa::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorIdParaAtualizacao(UUID id) {
        return jpa.buscarParaAtualizacao(id).map(ClienteRepositorioJpa::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(Documento documento) {
        return jpa.findByDocumento(documento.numero())
                .filter(e -> e.getTipoDocumento() == documento.tipo())
                .map(ClienteRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComDocumento(Documento documento) {
        return jpa.existsByDocumento(documento.numero());
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpa.findAllByOrderByNomeAsc().stream().map(ClienteRepositorioJpa::paraDominio).toList();
    }

    @Override
    public int zerarConsumoMensalDosPosPagos() {
        return jpa.zerarConsumo(TipoPlano.POS_PAGO);
    }

    private static Cliente paraDominio(ClienteEntity e) {
        return Cliente.reconstituir(e.getId(), e.getNome(), new Documento(e.getDocumento(), e.getTipoDocumento()),
                e.getTipoPlano(), e.getSaldo(), e.getLimiteMensal(), e.getConsumoMes(), e.isAtivo(), e.getCriadoEm());
    }
}

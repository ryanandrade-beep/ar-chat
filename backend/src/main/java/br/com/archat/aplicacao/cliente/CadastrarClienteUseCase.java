package br.com.archat.aplicacao.cliente;

import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.TransacaoRepositorio;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.excecao.RegraDeNegocioException;
import br.com.archat.dominio.financeiro.Transacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

public class CadastrarClienteUseCase {

    private final ClienteRepositorio clientes;
    private final TransacaoRepositorio transacoes;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public CadastrarClienteUseCase(ClienteRepositorio clientes, TransacaoRepositorio transacoes,
                                   UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.clientes = clientes;
        this.transacoes = transacoes;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public Cliente executar(Comando comando) {
        Documento documento = new Documento(comando.documento(), comando.tipoDocumento());
        Instant agora = relogio.instant();
        return unidadeDeTrabalho.executar(() -> {
            if (clientes.existeComDocumento(documento)) {
                throw new RegraDeNegocioException("Já existe um cliente com este documento");
            }
            Cliente cliente = Cliente.novo(comando.nome(), documento, comando.tipoPlano(), comando.valorInicial(), agora);
            clientes.salvar(cliente);
            registrarValorInicial(cliente, agora);
            return cliente;
        });
    }

    private void registrarValorInicial(Cliente cliente, Instant agora) {
        if (cliente.tipoPlano() == TipoPlano.PRE_PAGO && cliente.saldo().signum() > 0) {
            transacoes.salvar(Transacao.credito(cliente, cliente.saldo(), agora));
        } else if (cliente.tipoPlano() == TipoPlano.POS_PAGO) {
            transacoes.salvar(Transacao.ajusteDeLimite(cliente, agora));
        }
    }

    /** {@code valorInicial} é o saldo (pré-pago) ou o limite mensal (pós-pago). */
    public record Comando(String nome, String documento, TipoDocumento tipoDocumento, TipoPlano tipoPlano,
                          BigDecimal valorInicial) {
    }
}

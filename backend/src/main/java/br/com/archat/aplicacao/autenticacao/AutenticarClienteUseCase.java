package br.com.archat.aplicacao.autenticacao;

import br.com.archat.aplicacao.excecao.AcessoNegadoException;
import br.com.archat.aplicacao.excecao.NaoAutenticadoException;
import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.ServicoToken;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;

import java.time.Clock;
import java.time.Duration;

public class AutenticarClienteUseCase {

    private final ClienteRepositorio clientes;
    private final ServicoToken tokens;
    private final Clock relogio;
    private final Duration validadeToken;

    public AutenticarClienteUseCase(ClienteRepositorio clientes, ServicoToken tokens, Clock relogio,
                                    Duration validadeToken) {
        this.clientes = clientes;
        this.tokens = tokens;
        this.relogio = relogio;
        this.validadeToken = validadeToken;
    }

    public ClienteAutenticado executar(String numeroDocumento, TipoDocumento tipoDocumento) {
        Cliente cliente = clientes.buscarPorDocumento(new Documento(numeroDocumento, tipoDocumento))
                .orElseThrow(() -> new NaoAutenticadoException("Cliente não cadastrado"));
        if (!cliente.ativo()) {
            throw new AcessoNegadoException("Cliente inativo");
        }
        Sessao sessao = new Sessao(Sessao.Perfil.CLIENTE, cliente.id(), relogio.instant().plus(validadeToken));
        return new ClienteAutenticado(tokens.gerar(sessao), cliente);
    }

    public record ClienteAutenticado(String token, Cliente cliente) {
    }
}

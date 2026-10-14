package br.com.archat.aplicacao.conta;

import br.com.archat.aplicacao.autenticacao.AutenticarClienteUseCase.ClienteAutenticado;
import br.com.archat.aplicacao.autenticacao.Sessao;
import br.com.archat.aplicacao.porta.ClienteRepositorio;
import br.com.archat.aplicacao.porta.CodificadorSenha;
import br.com.archat.aplicacao.porta.ContaDeAcessoRepositorio;
import br.com.archat.aplicacao.porta.ServicoToken;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.cliente.Cliente;
import br.com.archat.dominio.cliente.ContaDeAcesso;
import br.com.archat.dominio.cliente.Documento;
import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Cadastro feito pela própria empresa na tela de "Criar conta": cria o cliente no plano pré-pago, sem saldo
 * (o administrador libera os créditos), e a conta de acesso com e-mail e senha. Já devolve a sessão aberta.
 */
public class CadastrarContaUseCase {

    public static final String VERSAO_TERMOS = "2026-10";

    private final ClienteRepositorio clientes;
    private final ContaDeAcessoRepositorio contas;
    private final CodificadorSenha codificador;
    private final ServicoToken tokens;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;
    private final Duration validadeToken;

    public CadastrarContaUseCase(ClienteRepositorio clientes, ContaDeAcessoRepositorio contas,
                                 CodificadorSenha codificador, ServicoToken tokens,
                                 UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio, Duration validadeToken) {
        this.clientes = clientes;
        this.contas = contas;
        this.codificador = codificador;
        this.tokens = tokens;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
        this.validadeToken = validadeToken;
    }

    public ClienteAutenticado executar(Comando comando) {
        Instant agora = relogio.instant();
        Documento documento = new Documento(comando.documento(), comando.tipoDocumento());
        String email = ContaDeAcesso.normalizarEmail(comando.email());
        ContaDeAcesso.validarSenha(comando.senha());
        if (!comando.senha().equals(comando.confirmacaoSenha())) {
            throw new RegraDeNegocioException("A confirmação não confere com a senha");
        }
        String senhaHash = codificador.codificar(comando.senha());
        Cliente cliente = unidadeDeTrabalho.executar(() -> {
            if (clientes.existeComDocumento(documento)) {
                throw new RegraDeNegocioException("Já existe uma conta com este documento");
            }
            if (contas.existeComEmail(email)) {
                throw new RegraDeNegocioException("Já existe uma conta com este e-mail");
            }
            Cliente novo = Cliente.novo(comando.nome(), documento, TipoPlano.PRE_PAGO, BigDecimal.ZERO, agora);
            clientes.salvar(novo);
            contas.salvar(ContaDeAcesso.nova(novo.id(), email, comando.telefone(), senhaHash, comando.aceitouTermos(),
                    VERSAO_TERMOS, agora));
            return novo;
        });
        Sessao sessao = new Sessao(Sessao.Perfil.CLIENTE, cliente.id(), agora.plus(validadeToken));
        return new ClienteAutenticado(tokens.gerar(sessao), cliente);
    }

    public record Comando(String nome, String email, String telefone, String documento, TipoDocumento tipoDocumento,
                          String senha, String confirmacaoSenha, boolean aceitouTermos) {
    }
}

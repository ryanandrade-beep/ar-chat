package br.com.archat.aplicacao.conta;

import br.com.archat.aplicacao.porta.CodificadorSenha;
import br.com.archat.aplicacao.porta.ContaDeAcessoRepositorio;
import br.com.archat.aplicacao.porta.EnvioDeEmail;
import br.com.archat.aplicacao.porta.UnidadeDeTrabalho;
import br.com.archat.dominio.cliente.ContaDeAcesso;
import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Optional;

/**
 * "Esqueci minha senha": envia por e-mail um código de 6 dígitos (válido por 15 minutos) e troca a senha
 * quando o código confere. A solicitação responde igual para e-mails cadastrados ou não, para não revelar
 * quem tem conta.
 */
public class RecuperarSenhaUseCase {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ContaDeAcessoRepositorio contas;
    private final CodificadorSenha codificador;
    private final EnvioDeEmail email;
    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Clock relogio;

    public RecuperarSenhaUseCase(ContaDeAcessoRepositorio contas, CodificadorSenha codificador, EnvioDeEmail email,
                                 UnidadeDeTrabalho unidadeDeTrabalho, Clock relogio) {
        this.contas = contas;
        this.codificador = codificador;
        this.email = email;
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.relogio = relogio;
    }

    public void solicitar(String enderecoEmail) {
        String normalizado;
        try {
            normalizado = ContaDeAcesso.normalizarEmail(enderecoEmail);
        } catch (RegraDeNegocioException e) {
            return;
        }
        String codigo = "%06d".formatted(ALEATORIO.nextInt(1_000_000));
        Optional<ContaDeAcesso> conta = unidadeDeTrabalho.executar(() -> contas.buscarPorEmail(normalizado).map(c -> {
            c.registrarCodigoDeRecuperacao(codificador.codificar(codigo), relogio.instant());
            contas.salvar(c);
            return c;
        }));
        conta.ifPresent(c -> email.enviar(c.email(), "A&R Chat – código para redefinir a senha",
                "Seu código é " + codigo + ". Ele vale por 15 minutos. Se não foi você, ignore este e-mail."));
    }

    public void redefinir(String enderecoEmail, String codigo, String novaSenha, String confirmacao) {
        ContaDeAcesso.validarSenha(novaSenha);
        if (!novaSenha.equals(confirmacao)) {
            throw new RegraDeNegocioException("A confirmação não confere com a senha");
        }
        String novaSenhaHash = codificador.codificar(novaSenha);
        String normalizado = ContaDeAcesso.normalizarEmail(enderecoEmail);
        RegraDeNegocioException erro = unidadeDeTrabalho.executar(() -> {
            ContaDeAcesso conta = contas.buscarPorEmail(normalizado)
                    .orElseThrow(() -> new RegraDeNegocioException("Código expirado ou inválido; solicite um novo"));
            boolean confere = conta.codigoHash() != null && codigo != null
                    && codificador.confere(codigo.strip(), conta.codigoHash());
            try {
                conta.redefinirSenha(confere, novaSenhaHash, relogio.instant());
                contas.salvar(conta);
                return null;
            } catch (RegraDeNegocioException e) {
                contas.salvar(conta);
                return e;
            }
        });
        if (erro != null) {
            throw erro;
        }
    }
}

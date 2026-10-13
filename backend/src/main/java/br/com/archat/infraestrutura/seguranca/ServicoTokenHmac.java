package br.com.archat.infraestrutura.seguranca;

import br.com.archat.aplicacao.autenticacao.Sessao;
import br.com.archat.aplicacao.porta.ServicoToken;
import br.com.archat.infraestrutura.config.ArChatPropriedades;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Token assinado com HMAC-SHA256 no formato {@code base64(perfil|id|expiracao).base64(assinatura)}.
 * Não guarda estado no servidor: qualquer alteração no conteúdo invalida a assinatura.
 */
@Component
class ServicoTokenHmac implements ServicoToken {

    private static final String ALGORITMO = "HmacSHA256";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODIFICADOR = Base64.getUrlDecoder();

    private final SecretKeySpec chave;
    private final Clock relogio;

    ServicoTokenHmac(ArChatPropriedades propriedades, Clock relogio) {
        String segredo = propriedades.token().segredo();
        if (segredo == null || segredo.length() < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("archat.token.segredo deve ter ao menos " + TAMANHO_MINIMO_SEGREDO + " caracteres");
        }
        this.chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), ALGORITMO);
        this.relogio = relogio;
    }

    @Override
    public String gerar(Sessao sessao) {
        String conteudo = "%s|%s|%d".formatted(sessao.perfil(), sessao.id(), sessao.expiraEm().getEpochSecond());
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);
        return CODIFICADOR.encodeToString(bytes) + "." + CODIFICADOR.encodeToString(assinar(bytes));
    }

    @Override
    public Optional<Sessao> validar(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 2) {
                return Optional.empty();
            }
            byte[] conteudo = DECODIFICADOR.decode(partes[0]);
            if (!MessageDigest.isEqual(assinar(conteudo), DECODIFICADOR.decode(partes[1]))) {
                return Optional.empty();
            }
            String[] campos = new String(conteudo, StandardCharsets.UTF_8).split("\\|");
            Sessao sessao = new Sessao(Sessao.Perfil.valueOf(campos[0]), UUID.fromString(campos[1]),
                    Instant.ofEpochSecond(Long.parseLong(campos[2])));
            return sessao.expiraEm().isAfter(relogio.instant()) ? Optional.of(sessao) : Optional.empty();
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private byte[] assinar(byte[] conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(chave);
            return mac.doFinal(conteudo);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar o token", e);
        }
    }
}

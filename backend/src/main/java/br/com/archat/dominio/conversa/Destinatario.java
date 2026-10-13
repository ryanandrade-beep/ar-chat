package br.com.archat.dominio.conversa;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Quem recebe as mensagens de um cliente: um contato (telefone) ou um grupo do WhatsApp (ID do grupo,
 * guardado no mesmo campo {@code telefone}, que é o endereço de envio). {@code aceitaOfertas} registra
 * o consentimento (LGPD) para receber campanhas.
 */
public record Destinatario(UUID id, UUID clienteId, TipoDestinatario tipo, String nome, String telefone,
                           boolean aceitaOfertas) {

    /** Formato atual ({@code 120363019502650977-group}) e o antigo ({@code 5511999999999-1623275280}). */
    private static final Pattern ID_GRUPO = Pattern.compile("\\d{10,25}-group|\\d{10,15}-\\d{6,12}");

    public Destinatario {
        Objects.requireNonNull(id);
        Objects.requireNonNull(clienteId);
        tipo = tipo == null ? TipoDestinatario.CONTATO : tipo;
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do destinatário é obrigatório");
        }
        if (nome.strip().length() > 120) {
            throw new RegraDeNegocioException("Nome do destinatário deve ter no máximo 120 caracteres");
        }
        nome = nome.strip();
        telefone = tipo == TipoDestinatario.GRUPO ? idDeGrupoValido(telefone) : telefoneValido(telefone);
    }

    private static String telefoneValido(String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 15) {
            throw new RegraDeNegocioException("Telefone deve ter entre 10 e 15 dígitos (com DDD)");
        }
        return digitos;
    }

    private static String idDeGrupoValido(String id) {
        String limpo = id == null ? "" : id.strip().toLowerCase();
        if (!ID_GRUPO.matcher(limpo).matches()) {
            throw new RegraDeNegocioException("ID de grupo inválido; use o formato 120363019502650977-group");
        }
        return limpo;
    }

    public static Destinatario novo(UUID clienteId, TipoDestinatario tipo, String nome, String telefone,
                                    boolean aceitaOfertas) {
        return new Destinatario(UUID.randomUUID(), clienteId, tipo, nome, telefone, aceitaOfertas);
    }

    /** Atalho para contatos (pessoas). */
    public static Destinatario novo(UUID clienteId, String nome, String telefone, boolean aceitaOfertas) {
        return novo(clienteId, TipoDestinatario.CONTATO, nome, telefone, aceitaOfertas);
    }

    public Destinatario alterar(TipoDestinatario novoTipo, String novoNome, String novoTelefone,
                                boolean novoAceitaOfertas) {
        return new Destinatario(id, clienteId, novoTipo, novoNome, novoTelefone, novoAceitaOfertas);
    }

    public boolean pertenceA(UUID clienteId) {
        return this.clienteId.equals(clienteId);
    }

    public boolean grupo() {
        return tipo == TipoDestinatario.GRUPO;
    }
}

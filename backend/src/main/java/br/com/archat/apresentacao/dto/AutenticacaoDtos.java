package br.com.archat.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public final class AutenticacaoDtos {

    private AutenticacaoDtos() {
    }

    public record ClientAuthRequest(@NotBlank String documentId, @NotBlank String documentType) {
    }

    public record ClientAuthResponse(String token, ClienteDtos.ClientResponse client) {
    }

    public record AdminAuthRequest(@NotBlank String login, @NotBlank String password) {
    }

    public record AdminAuthResponse(String token) {
    }
}

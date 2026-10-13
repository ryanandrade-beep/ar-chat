package br.com.archat.apresentacao.dto;

import br.com.archat.dominio.cliente.Cliente;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ClienteDtos {

    private ClienteDtos() {
    }

    /** {@code initialAmount} é o saldo inicial (prepaid) ou o limite mensal (postpaid). */
    public record CreateClientRequest(@NotBlank @Size(max = 120) String name, @NotBlank String documentId,
                                      @NotBlank String documentType, @NotBlank String planType,
                                      @DecimalMin("0.00") BigDecimal initialAmount) {
    }

    public record UpdateClientRequest(@NotBlank @Size(max = 120) String name, @NotNull Boolean active) {
    }

    public record CreditRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }

    public record LimitRequest(@NotNull @DecimalMin("0.00") BigDecimal limit) {
    }

    /** {@code limit} é obrigatório ao converter para postpaid. */
    public record ChangePlanRequest(@NotBlank String planType, BigDecimal limit) {
    }

    public record ClientResponse(UUID id, String name, String documentId, String documentType, String planType,
                                 BigDecimal balance, BigDecimal limit, BigDecimal monthlyUsage, BigDecimal available,
                                 boolean active, Instant createdAt) {

        public static ClientResponse de(Cliente c) {
            return new ClientResponse(c.id(), c.nome(), c.documento().numero(), c.documento().tipo().name(),
                    ApiEnums.plano(c.tipoPlano()), c.saldo(), c.limiteMensal(), c.consumoMes(), c.disponivel(),
                    c.ativo(), c.criadoEm());
        }
    }

    public record BalanceResponse(UUID clientId, String planType, BigDecimal balance, BigDecimal limit,
                                  BigDecimal monthlyUsage, BigDecimal available) {

        public static BalanceResponse de(Cliente c) {
            return new BalanceResponse(c.id(), ApiEnums.plano(c.tipoPlano()), c.saldo(), c.limiteMensal(),
                    c.consumoMes(), c.disponivel());
        }
    }
}

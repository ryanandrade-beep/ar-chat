package br.com.archat.apresentacao.seguranca;

import java.util.UUID;

/** Parâmetro de controller que exige um cliente autenticado. */
public record ClienteAutenticado(UUID id) {
}

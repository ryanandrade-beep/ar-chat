package br.com.archat.apresentacao.seguranca;

import java.util.UUID;

/** Parâmetro de controller que exige um administrador autenticado. */
public record AdministradorAutenticado(UUID id) {
}

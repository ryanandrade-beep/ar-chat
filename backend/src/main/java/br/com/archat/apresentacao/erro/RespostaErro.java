package br.com.archat.apresentacao.erro;

import java.time.Instant;
import java.util.Map;

public record RespostaErro(int status, String error, String message, Map<String, String> fields, Instant timestamp) {
}

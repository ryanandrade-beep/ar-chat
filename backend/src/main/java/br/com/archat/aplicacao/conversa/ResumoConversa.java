package br.com.archat.aplicacao.conversa;

import br.com.archat.dominio.conversa.Conversa;
import br.com.archat.dominio.conversa.Destinatario;

public record ResumoConversa(Conversa conversa, Destinatario destinatario) {
}

package br.com.archat.infraestrutura.email;

import br.com.archat.aplicacao.porta.EnvioDeEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Na primeira versão não há servidor de e-mail: a mensagem é escrita no log da aplicação, onde a demonstração
 * consulta o código. Um envio real (SMTP ou serviço de e-mail) entra como outra implementação desta porta.
 */
@Component
class EnvioDeEmailEmLog implements EnvioDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnvioDeEmailEmLog.class);

    @Override
    public void enviar(String para, String assunto, String texto) {
        log.info("E-mail para {} | {} | {}", para, assunto, texto);
    }
}

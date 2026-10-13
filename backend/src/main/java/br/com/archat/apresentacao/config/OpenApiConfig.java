package br.com.archat.apresentacao.config;

import br.com.archat.apresentacao.seguranca.AdministradorAutenticado;
import br.com.archat.apresentacao.seguranca.ClienteAutenticado;
import br.com.archat.apresentacao.seguranca.UsuarioAutenticado;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(ClienteAutenticado.class, AdministradorAutenticado.class,
                UsuarioAutenticado.class);
    }

    @Bean
    OpenAPI documentacao() {
        return new OpenAPI()
                .info(new Info().title("A&R Chat API").version("0.1.0")
                        .description("Atendimento ao cliente e campanhas de marketing pelo WhatsApp, com cobrança por mensagem e fila priorizada. "
                                + "Autentique-se em /auth (cliente) ou /admin/auth (administrador) e use o token em Authorize."))
                .components(new Components().addSecuritySchemes("token",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}

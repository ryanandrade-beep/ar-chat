package br.com.archat.apresentacao.config;

import br.com.archat.apresentacao.seguranca.ResolvedorDeSessao;
import br.com.archat.infraestrutura.config.ArChatPropriedades;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
class WebConfig implements WebMvcConfigurer {

    private final ResolvedorDeSessao resolvedorDeSessao;
    private final List<String> origensPermitidas;

    WebConfig(ResolvedorDeSessao resolvedorDeSessao, ArChatPropriedades propriedades) {
        this.resolvedorDeSessao = resolvedorDeSessao;
        this.origensPermitidas = propriedades.origensPermitidas();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolvedorDeSessao);
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/**")
                .allowedOrigins(origensPermitidas.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}

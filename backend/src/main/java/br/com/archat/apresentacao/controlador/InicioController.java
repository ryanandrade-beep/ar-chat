package br.com.archat.apresentacao.controlador;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** A raiz da API leva direto à documentação interativa (Swagger). */
@Controller
@Hidden
class InicioController {

    @GetMapping("/")
    String inicio() {
        return "redirect:/swagger-ui.html";
    }
}

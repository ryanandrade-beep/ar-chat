package br.com.archat.infraestrutura.config;

import br.com.archat.aplicacao.administrador.GerenciarAdministradoresUseCase;
import br.com.archat.aplicacao.cliente.ReiniciarConsumoMensalUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Tarefas que não vêm de requisições: o administrador inicial e o fechamento mensal do pós-pago. */
@Configuration
@EnableScheduling
class TarefasDoSistema implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TarefasDoSistema.class);

    private final GerenciarAdministradoresUseCase administradores;
    private final ReiniciarConsumoMensalUseCase reiniciarConsumoMensal;
    private final ArChatPropriedades.AdministradorInicial administradorInicial;

    TarefasDoSistema(GerenciarAdministradoresUseCase administradores,
                     ReiniciarConsumoMensalUseCase reiniciarConsumoMensal, ArChatPropriedades propriedades) {
        this.administradores = administradores;
        this.reiniciarConsumoMensal = reiniciarConsumoMensal;
        this.administradorInicial = propriedades.administradorInicial();
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (administradores.criarInicialSeNecessario(administradorInicial.nome(), administradorInicial.login(),
                administradorInicial.senha())) {
            log.warn("Administrador inicial '{}' criado; troque a senha padrão em produção", administradorInicial.login());
        }
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "America/Sao_Paulo")
    void reiniciarConsumoDoMes() {
        log.info("Consumo mensal zerado para {} cliente(s) pós-pago(s)", reiniciarConsumoMensal.executar());
    }
}

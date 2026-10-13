package br.com.archat.apresentacao.controlador;

import br.com.archat.aplicacao.monitoramento.MonitorarFilaUseCase;
import br.com.archat.apresentacao.dto.RelatorioDtos.QueueStatus;
import br.com.archat.apresentacao.seguranca.AdministradorAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/queue")
@Tag(name = "Monitoramento da fila")
class FilaController {

    private final MonitorarFilaUseCase monitorar;

    FilaController(MonitorarFilaUseCase monitorar) {
        this.monitorar = monitorar;
    }

    @GetMapping("/status")
    @Operation(summary = "Situação da fila agora: pendentes por prioridade, enviadas, falhas e espera média (admin)")
    QueueStatus status(AdministradorAutenticado administrador) {
        return QueueStatus.de(monitorar.emitir());
    }
}

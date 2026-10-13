package br.com.archat.apresentacao.controlador;

import br.com.archat.aplicacao.atendimento.GerenciarAtendimentoUseCase;
import br.com.archat.aplicacao.atendimento.ReceberMensagemUseCase;
import br.com.archat.aplicacao.conversa.ConsultarConversasUseCase;
import br.com.archat.aplicacao.mensagem.ConsultarMensagensUseCase;
import br.com.archat.aplicacao.porta.FiltroMensagens;
import br.com.archat.apresentacao.dto.ApiEnums;
import br.com.archat.apresentacao.dto.ConversaDtos.ConversationResponse;
import br.com.archat.apresentacao.dto.MensagemDtos.SimulatedReplyRequest;
import br.com.archat.apresentacao.dto.MensagemDtos.MessageResponse;
import br.com.archat.apresentacao.seguranca.ClienteAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
@Tag(name = "Atendimento", description = "Conversas e o status do atendimento: aguardando, em andamento, em espera e finalizado")
class ConversaController {

    private final ConsultarConversasUseCase conversas;
    private final ConsultarMensagensUseCase mensagens;
    private final GerenciarAtendimentoUseCase atendimento;
    private final ReceberMensagemUseCase receber;

    ConversaController(ConsultarConversasUseCase conversas, ConsultarMensagensUseCase mensagens,
                       GerenciarAtendimentoUseCase atendimento, ReceberMensagemUseCase receber) {
        this.conversas = conversas;
        this.mensagens = mensagens;
        this.atendimento = atendimento;
        this.receber = receber;
    }

    @GetMapping
    @Operation(summary = "Conversas do cliente, da mais recente para a mais antiga; filtre por status: "
            + "waiting, in_progress, on_hold ou finished")
    List<ConversationResponse> listar(ClienteAutenticado cliente, @RequestParam(required = false) String search,
                                      @RequestParam(required = false) String status) {
        return conversas.listar(cliente.id(), search, status == null ? null : ApiEnums.atendimento(status)).stream()
                .map(ConversationResponse::de).toList();
    }

    @GetMapping("/summary")
    @Operation(summary = "Quantidade de conversas em cada status (para as abas da tela de atendimento)")
    Map<String, Long> resumo(ClienteAutenticado cliente) {
        Map<String, Long> resumo = new LinkedHashMap<>();
        atendimento.resumo(cliente.id()).forEach((status, total) -> resumo.put(ApiEnums.atendimento(status), total));
        return resumo;
    }

    @PostMapping("/{id}/hold")
    @Operation(summary = "Coloca o atendimento em espera")
    ConversationResponse colocarEmEspera(ClienteAutenticado cliente, @PathVariable UUID id) {
        atendimento.colocarEmEspera(cliente.id(), id);
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Retoma um atendimento em espera")
    ConversationResponse retomar(ClienteAutenticado cliente, @PathVariable UUID id) {
        atendimento.retomar(cliente.id(), id);
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finaliza o atendimento; uma nova mensagem do cliente final abre outro")
    ConversationResponse finalizar(ClienteAutenticado cliente, @PathVariable UUID id) {
        atendimento.finalizar(cliente.id(), id);
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Marca as mensagens recebidas da conversa como lidas (zera o contador)")
    ConversationResponse marcarComoLida(ClienteAutenticado cliente, @PathVariable UUID id) {
        atendimento.marcarComoLida(cliente.id(), id);
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @PostMapping("/{id}/simulate-reply")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simula uma mensagem do cliente final nesta conversa (para testes e demonstração, "
            + "sem depender do webhook)")
    ConversationResponse simularResposta(ClienteAutenticado cliente, @PathVariable UUID id,
                                         @Valid @RequestBody SimulatedReplyRequest r) {
        receber.simularResposta(cliente.id(), id, r.content());
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de uma conversa")
    ConversationResponse buscar(ClienteAutenticado cliente, @PathVariable UUID id) {
        return ConversationResponse.de(conversas.buscar(cliente.id(), id));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Mensagens de uma conversa, em ordem cronológica")
    List<MessageResponse> mensagens(ClienteAutenticado cliente, @PathVariable UUID id) {
        return mensagens.listar(new FiltroMensagens(cliente.id(), id, null, null)).stream()
                .map(MessageResponse::de).toList();
    }
}

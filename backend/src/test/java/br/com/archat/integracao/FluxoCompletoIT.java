package br.com.archat.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Percorre a API de ponta a ponta: admin cadastra o cliente, cliente envia e o worker entrega. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoCompletoIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void fluxoDeEnvioComCobrancaFilaERelatorios() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");

        String clienteId = corpo(executar(post("/clients"), admin, """
                {"name": "Empresa ABC", "documentId": "11.222.333/0001-81", "documentType": "CNPJ",
                 "planType": "prepaid", "initialAmount": 0.75}""").andExpect(status().isCreated()), "$.id");

        String cliente = token(post("/auth"), """
                {"documentId": "11222333000181", "documentType": "CNPJ"}""");

        String destinatarioId = corpo(executar(post("/recipients"), cliente, """
                {"name": "Maria Silva", "phone": "(44) 99999-0000"}""").andExpect(status().isCreated()), "$.id");

        String mensagemId = corpo(executar(post("/messages"), cliente, """
                {"recipientId": "%s", "content": "Olá, como vai?", "priority": "urgent"}""".formatted(destinatarioId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("queued"))
                .andExpect(jsonPath("$.cost").value(0.5))
                .andExpect(jsonPath("$.currentBalance").value(0.25)), "$.id");

        executar(post("/messages"), cliente, """
                {"recipientId": "%s", "content": "Outra", "priority": "urgent"}""".formatted(destinatarioId))
                .andExpect(status().isPaymentRequired());

        aguardarStatus(cliente, mensagemId, "delivered");

        executar(get("/conversations"), cliente, null)
                .andExpect(jsonPath("$[0].recipientName").value("Maria Silva"))
                .andExpect(jsonPath("$[0].lastMessageContent").value("Olá, como vai?"));

        executar(get("/clients/" + clienteId + "/balance"), cliente, null)
                .andExpect(jsonPath("$.available").value(0.25));

        String hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        executar(get("/reports/attendances?from=%s&to=%s".formatted(hoje, hoje)), cliente, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
        executar(get("/reports/attendances?from=%s&to=%s&format=csv".formatted(hoje, hoje)), cliente, null)
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Type", org.hamcrest.Matchers.startsWith("text/csv")));
        executar(get("/reports/revenue?from=%s&to=%s".formatted(hoje, hoje)), admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clients[?(@.name == 'Empresa ABC')].netRevenue").value(0.5));
        executar(get("/reports/revenue?from=%s&to=%s".formatted(hoje, hoje)), cliente, null)
                .andExpect(status().isForbidden());
        executar(get("/reports/campaigns?from=%s&to=%s".formatted(hoje, hoje)), cliente, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns").isEmpty());
        executar(get("/queue/status"), admin, null)
                .andExpect(jsonPath("$.sent").isNumber());
    }

    @Test
    void campanhaDisparaParaQuemAceitaOfertasEAcompanhaOAndamento() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(post("/clients"), admin, """
                {"name": "Supermercado São Pedro", "documentId": "529.982.247-25", "documentType": "CPF",
                 "planType": "prepaid", "initialAmount": 10}""").andExpect(status().isCreated());
        String mercado = token(post("/auth"), """
                {"documentId": "52998224725", "documentType": "CPF"}""");
        for (int i = 1; i <= 3; i++) {
            executar(post("/recipients"), mercado, """
                    {"name": "Cliente %d", "phone": "4498888000%d", "acceptsMarketing": %b}""".formatted(i, i, i != 3))
                    .andExpect(status().isCreated());
        }

        String campanhaId = corpo(executar(post("/campaigns"), mercado, """
                {"name": "Ofertas da semana", "content": "Picanha R$ 49,90/kg", "priority": "normal", "allOptedIn": true}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRecipients").value(2))
                .andExpect(jsonPath("$.totalCost").value(0.5))
                .andExpect(jsonPath("$.currentBalance").value(9.5)), "$.id");

        for (int i = 0; i < 50; i++) {
            String entregues = corpo(executar(get("/campaigns/" + campanhaId), mercado, null), "$.messagesByStatus.delivered");
            if ("2".equals(entregues)) {
                String hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
                executar(get("/reports/campaigns?from=%s&to=%s".formatted(hoje, hoje)), mercado, null)
                        .andExpect(jsonPath("$.campaigns[0].delivered").value(2))
                        .andExpect(jsonPath("$.campaigns[0].deliveryRate").value(100.0))
                        .andExpect(jsonPath("$.campaigns[0].costPerDelivered").value(0.25));
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("As mensagens da campanha não foram entregues");
    }

    @Test
    void enviaMensagemParaGrupoDoWhatsApp() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(post("/clients"), admin, """
                {"name": "Loja do Grupo", "documentId": "111.444.777-35", "documentType": "CPF",
                 "planType": "postpaid", "initialAmount": 5}""").andExpect(status().isCreated());
        String loja = token(post("/auth"), """
                {"documentId": "11144477735", "documentType": "CPF"}""");

        String grupoId = corpo(executar(post("/recipients"), loja, """
                {"type": "group", "name": "Clientes VIP", "phone": "120363019502650977-group"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("group"))
                .andExpect(jsonPath("$.phone").value("120363019502650977-group")), "$.id");

        executar(post("/recipients"), loja, """
                {"type": "group", "name": "Inválido", "phone": "grupo-vip"}""")
                .andExpect(status().isUnprocessableContent());

        executar(post("/messages"), loja, """
                {"recipientId": "%s", "content": "Promoção para o grupo", "priority": "normal"}""".formatted(grupoId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cost").value(0.25));
    }

    @Test
    void atendimentoCompletoComWebhooksDaZApi() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(post("/clients"), admin, """
                {"name": "Padaria", "documentId": "935.411.347-80", "documentType": "CPF",
                 "planType": "prepaid", "initialAmount": 5}""").andExpect(status().isCreated());
        String padaria = token(post("/auth"), """
                {"documentId": "93541134780", "documentType": "CPF"}""");
        executar(post("/recipients"), padaria, """
                {"name": "Joana", "phone": "5544977776666"}""").andExpect(status().isCreated());

        // webhook "Ao receber" — corpo no formato do exemplo da documentação da Z-API
        mvc.perform(post("/webhooks/zapi/segredo-de-teste/received").contentType(MediaType.APPLICATION_JSON).content("""
                {"isStatusReply": false, "senderLid": "81896604192873@lid", "connectedPhone": "554499999999",
                 "waitingMessage": false, "isEdit": false, "isGroup": false, "isNewsletter": false,
                 "instanceId": "A20DA9C0183A2D35A260F53F5D2B9244", "messageId": "MSG-RECEBIDA-1",
                 "phone": "5544977776666", "fromMe": false, "momment": 1632228638000, "status": "RECEIVED",
                 "chatName": "name", "senderPhoto": "https://", "senderName": "Joana", "participantPhone": null,
                 "participantLid": null, "photo": "https://", "broadcast": false, "type": "ReceivedCallback",
                 "text": {"message": "Tem pão de queijo hoje?"}}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true));

        executar(get("/conversations/summary"), padaria, null).andExpect(jsonPath("$.waiting").value(1));
        String conversaId = corpo(executar(get("/conversations?status=waiting"), padaria, null)
                .andExpect(jsonPath("$[0].unreadCount").value(1))
                .andExpect(jsonPath("$[0].lastMessageContent").value("Tem pão de queijo hoje?")), "$[0].id");

        String respostaId = corpo(executar(post("/messages"), padaria, """
                {"conversationId": "%s", "content": "Tem sim, saindo do forno!", "priority": "normal"}""".formatted(conversaId))
                .andExpect(status().isCreated()), "$.id");
        executar(get("/conversations/" + conversaId), padaria, null).andExpect(jsonPath("$.status").value("in_progress"));

        executar(post("/conversations/" + conversaId + "/hold"), padaria, null).andExpect(jsonPath("$.status").value("on_hold"));
        executar(post("/conversations/" + conversaId + "/resume"), padaria, null).andExpect(jsonPath("$.status").value("in_progress"));
        executar(post("/conversations/" + conversaId + "/read"), padaria, null).andExpect(jsonPath("$.unreadCount").value(0));

        // webhook "Status da mensagem": aguarda o envio simulado e confirma a leitura pelo id externo
        String idExterno = null;
        for (int i = 0; i < 50 && idExterno == null; i++) {
            Thread.sleep(100);
            idExterno = mensagemEnviada(respostaId);
        }
        assertThat(idExterno).isNotNull();
        mvc.perform(post("/webhooks/zapi/segredo-de-teste/status").contentType(MediaType.APPLICATION_JSON).content("""
                {"instanceId": "instance.id", "status": "READ", "ids": ["%s"], "momment": 1632234645000,
                 "phoneDevice": 0, "phone": "5544977776666", "type": "MessageStatusCallback", "isGroup": false}"""
                .formatted(idExterno)))
                .andExpect(status().isOk());
        executar(get("/messages/" + respostaId + "/status"), padaria, null).andExpect(jsonPath("$.status").value("read"));

        executar(post("/conversations/" + conversaId + "/finish"), padaria, null).andExpect(jsonPath("$.status").value("finished"));
        String hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString();
        executar(get("/reports/attendances?from=%s&to=%s".formatted(hoje, hoje)), padaria, null)
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.attendances[0].recipient").value("Joana"))
                .andExpect(jsonPath("$.attendances[0].startedBy").value("customer"));

        mvc.perform(post("/webhooks/zapi/segredo-errado/received").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void canaisMostramWhatsAppBusinessEmBreve() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(get("/channels"), admin, null)
                .andExpect(jsonPath("$[0].id").value("whatsapp_web"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value("whatsapp_business"))
                .andExpect(jsonPath("$[1].badge").value("Em breve"));
    }

    @org.springframework.beans.factory.annotation.Autowired
    private br.com.archat.aplicacao.porta.MensagemRepositorio repositorioDeMensagens;

    private String mensagemEnviada(String mensagemId) {
        return repositorioDeMensagens.buscarPorId(java.util.UUID.fromString(mensagemId))
                .filter(m -> m.status() == br.com.archat.dominio.mensagem.StatusMensagem.ENVIADA
                        || m.status() == br.com.archat.dominio.mensagem.StatusMensagem.ENTREGUE)
                .map(br.com.archat.dominio.mensagem.Mensagem::idExterno)
                .orElse(null);
    }

    @Test
    void rotasProtegidasExigemTokenEPerfilCorreto() throws Exception {
        mvc.perform(get("/clients")).andExpect(status().isUnauthorized());
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(get("/recipients"), admin, null).andExpect(status().isForbidden());
    }

    @Test
    void validaDadosDeEntrada() throws Exception {
        String admin = token(post("/admin/auth"), """
                {"login": "admin", "password": "admin12345"}""");
        executar(post("/clients"), admin, """
                {"name": "X", "documentId": "123", "documentType": "CPF", "planType": "prepaid"}""")
                .andExpect(status().isUnprocessableContent());
        executar(post("/clients"), admin, """
                {"name": "", "documentType": "CPF"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    private void aguardarStatus(String token, String mensagemId, String esperado) throws Exception {
        String atual = null;
        for (int i = 0; i < 50 && !esperado.equals(atual); i++) {
            atual = corpo(executar(get("/messages/" + mensagemId + "/status"), token, null), "$.status");
            if (!esperado.equals(atual)) {
                Thread.sleep(100);
            }
        }
        assertThat(atual).isEqualTo(esperado);
    }

    private String token(MockHttpServletRequestBuilder requisicao, String json) throws Exception {
        return corpo(mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()), "$.token");
    }

    private ResultActions executar(MockHttpServletRequestBuilder requisicao, String token, String json) throws Exception {
        requisicao.header("Authorization", "Bearer " + token);
        if (json != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(requisicao);
    }

    private static String corpo(ResultActions resultado, String caminho) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), caminho).toString();
    }
}

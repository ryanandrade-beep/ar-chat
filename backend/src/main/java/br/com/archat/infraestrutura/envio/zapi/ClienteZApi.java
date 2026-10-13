package br.com.archat.infraestrutura.envio.zapi;

import br.com.archat.infraestrutura.config.ArChatPropriedades;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP da Z-API (https://developer.z-api.io). Todas as rotas seguem
 * {@code /instances/{instancia}/token/{token}/...} com o cabeçalho {@code Client-Token}.
 */
@Component
@ConditionalOnProperty(name = "archat.envio.provedor", havingValue = "zapi")
class ClienteZApi {

    private final RestClient http;

    ClienteZApi(ArChatPropriedades propriedades) {
        ArChatPropriedades.ZApi zapi = propriedades.zapi();
        if (vazio(zapi.instancia()) || vazio(zapi.token()) || vazio(zapi.clientToken())) {
            throw new IllegalStateException("Com archat.envio.provedor=zapi, informe ARCHAT_ZAPI_INSTANCIA, ARCHAT_ZAPI_TOKEN e ARCHAT_ZAPI_CLIENT_TOKEN");
        }
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(zapi.timeout());
        fabrica.setReadTimeout(zapi.timeout());
        this.http = RestClient.builder()
                .baseUrl(zapi.urlBase() + "/instances/" + zapi.instancia() + "/token/" + zapi.token())
                .defaultHeader("Client-Token", zapi.clientToken())
                .requestFactory(fabrica)
                .build();
    }

    RespostaEnvio enviarTexto(String telefone, String mensagem) {
        return http.post().uri("/send-text").contentType(MediaType.APPLICATION_JSON)
                .body(new EnvioTexto(telefone, mensagem))
                .retrieve().body(RespostaEnvio.class);
    }

    RespostaStatus status() {
        return http.get().uri("/status").retrieve().body(RespostaStatus.class);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> qrCodeImagem() {
        return http.get().uri("/qr-code/image").retrieve().body(Map.class);
    }

    List<GrupoZApi> grupos(int pagina, int tamanhoPagina) {
        GrupoZApi[] grupos = http.get().uri("/groups?page={pagina}&pageSize={tamanho}", pagina, tamanhoPagina)
                .retrieve().body(GrupoZApi[].class);
        return grupos == null ? List.of() : List.of(grupos);
    }

    void desconectar() {
        http.get().uri("/disconnect").retrieve().toBodilessEntity();
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    record EnvioTexto(String phone, String message) {
    }

    record RespostaEnvio(String zaapId, String messageId) {
    }

    record RespostaStatus(Boolean connected, String error, Boolean smartphoneConnected) {
    }

    record GrupoZApi(String phone, String name, Boolean isGroup) {
    }
}

package br.com.archat.apresentacao.controlador;

import br.com.archat.aplicacao.porta.ConexaoWhatsApp.GrupoWhatsApp;
import br.com.archat.aplicacao.porta.ConexaoWhatsApp.SituacaoConexao;
import br.com.archat.aplicacao.whatsapp.GerenciarConexaoWhatsAppUseCase;
import br.com.archat.apresentacao.seguranca.AdministradorAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/whatsapp")
@Tag(name = "WhatsApp", description = "Conexão do número de envio (Z-API)")
class WhatsAppController {

    private final GerenciarConexaoWhatsAppUseCase conexao;

    WhatsAppController(GerenciarConexaoWhatsAppUseCase conexao) {
        this.conexao = conexao;
    }

    @GetMapping("/status")
    @Operation(summary = "Situação da conexão com o WhatsApp (admin)")
    ConnectionResponse status(AdministradorAutenticado administrador) {
        return ConnectionResponse.de(conexao.situacao());
    }

    @GetMapping("/qr-code")
    @Operation(summary = "QR Code para conectar o número (admin); expira em poucos segundos, consulte de novo")
    QrCodeResponse qrCode(AdministradorAutenticado administrador) {
        return new QrCodeResponse(conexao.qrCode());
    }

    @GetMapping("/groups")
    @Operation(summary = "Grupos de que o número participa, com o ID para cadastrar como destinatário (admin)")
    List<GroupResponse> grupos(AdministradorAutenticado administrador, @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "50") int size) {
        return conexao.listarGrupos(page, size).stream().map(GroupResponse::de).toList();
    }

    @PostMapping("/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desconecta o número (admin)")
    void desconectar(AdministradorAutenticado administrador) {
        conexao.desconectar();
    }

    record ConnectionResponse(String provider, boolean connected, boolean phoneOnline, String detail) {

        static ConnectionResponse de(SituacaoConexao s) {
            return new ConnectionResponse(s.provedor(), s.conectado(), s.celularOnline(), s.detalhe());
        }
    }

    record QrCodeResponse(String image) {
    }

    record GroupResponse(String id, String name) {

        static GroupResponse de(GrupoWhatsApp g) {
            return new GroupResponse(g.id(), g.nome());
        }
    }
}

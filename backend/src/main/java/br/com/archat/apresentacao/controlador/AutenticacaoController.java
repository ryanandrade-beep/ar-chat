package br.com.archat.apresentacao.controlador;

import br.com.archat.aplicacao.autenticacao.AutenticarAdministradorUseCase;
import br.com.archat.aplicacao.autenticacao.AutenticarClienteUseCase;
import br.com.archat.apresentacao.dto.ApiEnums;
import br.com.archat.apresentacao.dto.AutenticacaoDtos.AdminAuthRequest;
import br.com.archat.apresentacao.dto.AutenticacaoDtos.AdminAuthResponse;
import br.com.archat.apresentacao.dto.AutenticacaoDtos.ClientAuthRequest;
import br.com.archat.apresentacao.dto.AutenticacaoDtos.ClientAuthResponse;
import br.com.archat.apresentacao.dto.ClienteDtos.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticação")
class AutenticacaoController {

    private final AutenticarClienteUseCase autenticarCliente;
    private final AutenticarAdministradorUseCase autenticarAdministrador;

    AutenticacaoController(AutenticarClienteUseCase autenticarCliente,
                           AutenticarAdministradorUseCase autenticarAdministrador) {
        this.autenticarCliente = autenticarCliente;
        this.autenticarAdministrador = autenticarAdministrador;
    }

    @PostMapping("/auth")
    @Operation(summary = "Autentica o cliente pelo CPF/CNPJ")
    ClientAuthResponse autenticarCliente(@Valid @RequestBody ClientAuthRequest requisicao) {
        var autenticado = autenticarCliente.executar(requisicao.documentId(), ApiEnums.documento(requisicao.documentType()));
        return new ClientAuthResponse(autenticado.token(), ClientResponse.de(autenticado.cliente()));
    }

    @PostMapping("/admin/auth")
    @Operation(summary = "Autentica o administrador por login e senha")
    AdminAuthResponse autenticarAdministrador(@Valid @RequestBody AdminAuthRequest requisicao) {
        return new AdminAuthResponse(autenticarAdministrador.executar(requisicao.login(), requisicao.password()));
    }
}

package br.com.archat.infraestrutura.persistencia.entidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conta_acesso")
public class ContaDeAcessoEntity {

    @Id
    @Column(name = "cliente_id")
    private UUID clienteId;
    private String email;
    private String telefone;
    @Column(name = "senha_hash")
    private String senhaHash;
    @Column(name = "versao_termos")
    private String versaoTermos;
    @Column(name = "termos_aceitos_em")
    private Instant termosAceitosEm;
    @Column(name = "codigo_hash")
    private String codigoHash;
    @Column(name = "codigo_expira_em")
    private Instant codigoExpiraEm;
    @Column(name = "tentativas_codigo")
    private int tentativasCodigo;

    protected ContaDeAcessoEntity() {
    }

    public ContaDeAcessoEntity(UUID clienteId, String email, String telefone, String senhaHash, String versaoTermos,
                               Instant termosAceitosEm, String codigoHash, Instant codigoExpiraEm, int tentativasCodigo) {
        this.clienteId = clienteId;
        this.email = email;
        this.telefone = telefone;
        this.senhaHash = senhaHash;
        this.versaoTermos = versaoTermos;
        this.termosAceitosEm = termosAceitosEm;
        this.codigoHash = codigoHash;
        this.codigoExpiraEm = codigoExpiraEm;
        this.tentativasCodigo = tentativasCodigo;
    }

    public UUID getClienteId() { return clienteId; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getSenhaHash() { return senhaHash; }
    public String getVersaoTermos() { return versaoTermos; }
    public Instant getTermosAceitosEm() { return termosAceitosEm; }
    public String getCodigoHash() { return codigoHash; }
    public Instant getCodigoExpiraEm() { return codigoExpiraEm; }
    public int getTentativasCodigo() { return tentativasCodigo; }
}

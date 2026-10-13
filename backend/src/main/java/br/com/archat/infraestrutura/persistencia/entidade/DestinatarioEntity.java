package br.com.archat.infraestrutura.persistencia.entidade;

import br.com.archat.dominio.conversa.TipoDestinatario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "destinatario")
public class DestinatarioEntity {

    @Id
    private UUID id;
    @Column(name = "cliente_id")
    private UUID clienteId;
    @Enumerated(EnumType.STRING)
    private TipoDestinatario tipo;
    private String nome;
    private String telefone;
    @Column(name = "aceita_ofertas")
    private boolean aceitaOfertas;

    protected DestinatarioEntity() {
    }

    public DestinatarioEntity(UUID id, UUID clienteId, TipoDestinatario tipo, String nome, String telefone,
                              boolean aceitaOfertas) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.nome = nome;
        this.telefone = telefone;
        this.aceitaOfertas = aceitaOfertas;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public TipoDestinatario getTipo() { return tipo; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public boolean isAceitaOfertas() { return aceitaOfertas; }
}

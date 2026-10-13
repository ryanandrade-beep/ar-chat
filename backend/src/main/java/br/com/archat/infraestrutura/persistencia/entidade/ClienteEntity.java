package br.com.archat.infraestrutura.persistencia.entidade;

import br.com.archat.dominio.cliente.TipoDocumento;
import br.com.archat.dominio.cliente.TipoPlano;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cliente")
public class ClienteEntity {

    @Id
    private UUID id;
    private String nome;
    private String documento;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDocumento;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plano")
    private TipoPlano tipoPlano;
    private BigDecimal saldo;
    @Column(name = "limite_mensal")
    private BigDecimal limiteMensal;
    @Column(name = "consumo_mes")
    private BigDecimal consumoMes;
    private boolean ativo;
    @Column(name = "criado_em")
    private Instant criadoEm;

    protected ClienteEntity() {
    }

    public ClienteEntity(UUID id, String nome, String documento, TipoDocumento tipoDocumento, TipoPlano tipoPlano,
                         BigDecimal saldo, BigDecimal limiteMensal, BigDecimal consumoMes, boolean ativo,
                         Instant criadoEm) {
        this.id = id;
        this.nome = nome;
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.tipoPlano = tipoPlano;
        this.saldo = saldo;
        this.limiteMensal = limiteMensal;
        this.consumoMes = consumoMes;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public TipoPlano getTipoPlano() { return tipoPlano; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getLimiteMensal() { return limiteMensal; }
    public BigDecimal getConsumoMes() { return consumoMes; }
    public boolean isAtivo() { return ativo; }
    public Instant getCriadoEm() { return criadoEm; }
}

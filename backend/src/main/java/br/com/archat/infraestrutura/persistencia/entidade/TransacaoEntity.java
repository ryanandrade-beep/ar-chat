package br.com.archat.infraestrutura.persistencia.entidade;

import br.com.archat.dominio.financeiro.TipoTransacao;
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
@Table(name = "transacao")
public class TransacaoEntity {

    @Id
    private UUID id;
    @Column(name = "cliente_id")
    private UUID clienteId;
    @Column(name = "mensagem_id")
    private UUID mensagemId;
    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;
    private BigDecimal valor;
    @Column(name = "saldo_apos")
    private BigDecimal saldoApos;
    @Column(name = "data_hora")
    private Instant dataHora;

    protected TransacaoEntity() {
    }

    public TransacaoEntity(UUID id, UUID clienteId, UUID mensagemId, TipoTransacao tipo, BigDecimal valor,
                           BigDecimal saldoApos, Instant dataHora) {
        this.id = id;
        this.clienteId = clienteId;
        this.mensagemId = mensagemId;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoApos = saldoApos;
        this.dataHora = dataHora;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public UUID getMensagemId() { return mensagemId; }
    public TipoTransacao getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldoApos() { return saldoApos; }
    public Instant getDataHora() { return dataHora; }
}

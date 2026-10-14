package br.com.archat.infraestrutura.persistencia.entidade;

import br.com.archat.dominio.mensagem.Midia;
import br.com.archat.dominio.mensagem.Prioridade;
import br.com.archat.dominio.mensagem.TipoMidia;
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
@Table(name = "campanha")
public class CampanhaEntity {

    @Id
    private UUID id;
    @Column(name = "cliente_id")
    private UUID clienteId;
    private String nome;
    private String conteudo;
    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;
    @Column(name = "total_destinatarios")
    private int totalDestinatarios;
    @Column(name = "custo_total")
    private BigDecimal custoTotal;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_midia")
    private TipoMidia tipoMidia;
    @Column(name = "url_midia")
    private String urlMidia;
    @Column(name = "nome_arquivo")
    private String nomeArquivo;
    @Column(name = "criada_em")
    private Instant criadaEm;

    protected CampanhaEntity() {
    }

    public CampanhaEntity(UUID id, UUID clienteId, String nome, String conteudo, Prioridade prioridade,
                          int totalDestinatarios, BigDecimal custoTotal, Instant criadaEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.nome = nome;
        this.conteudo = conteudo;
        this.prioridade = prioridade;
        this.totalDestinatarios = totalDestinatarios;
        this.custoTotal = custoTotal;
        this.criadaEm = criadaEm;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getNome() { return nome; }
    public String getConteudo() { return conteudo; }
    public Prioridade getPrioridade() { return prioridade; }
    public int getTotalDestinatarios() { return totalDestinatarios; }
    public BigDecimal getCustoTotal() { return custoTotal; }
    public Instant getCriadaEm() { return criadaEm; }

    public void definirMidia(Midia midia) {
        this.tipoMidia = midia == null ? null : midia.tipo();
        this.urlMidia = midia == null ? null : midia.url();
        this.nomeArquivo = midia == null ? null : midia.nomeArquivo();
    }

    public Midia getMidia() {
        return tipoMidia == null ? null : new Midia(tipoMidia, urlMidia, nomeArquivo);
    }
}

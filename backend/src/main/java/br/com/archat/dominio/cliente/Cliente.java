package br.com.archat.dominio.cliente;

import br.com.archat.dominio.excecao.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Empresa (PF ou PJ) que usa a plataforma para conversar com os seus clientes. */
public class Cliente {

    private final UUID id;
    private String nome;
    private final Documento documento;
    private TipoPlano tipoPlano;
    private BigDecimal saldo;
    private BigDecimal limiteMensal;
    private BigDecimal consumoMes;
    private boolean ativo;
    private final Instant criadoEm;

    private Cliente(UUID id, String nome, Documento documento, TipoPlano tipoPlano, BigDecimal saldo,
                    BigDecimal limiteMensal, BigDecimal consumoMes, boolean ativo, Instant criadoEm) {
        this.id = Objects.requireNonNull(id);
        this.nome = nomeValido(nome);
        this.documento = Objects.requireNonNull(documento);
        this.tipoPlano = Objects.requireNonNull(tipoPlano, "Tipo de plano é obrigatório");
        this.saldo = dinheiro(saldo);
        this.limiteMensal = dinheiro(limiteMensal);
        this.consumoMes = dinheiro(consumoMes);
        this.ativo = ativo;
        this.criadoEm = Objects.requireNonNull(criadoEm);
    }

    /**
     * Cadastra um novo cliente. O valor inicial é o saldo, no pré-pago, ou o limite mensal, no pós-pago.
     */
    public static Cliente novo(String nome, Documento documento, TipoPlano tipoPlano, BigDecimal valorInicial,
                               Instant agora) {
        BigDecimal valor = naoNegativo(valorInicial == null ? BigDecimal.ZERO : valorInicial, "Valor inicial");
        boolean prePago = tipoPlano == TipoPlano.PRE_PAGO;
        return new Cliente(UUID.randomUUID(), nome, documento, tipoPlano,
                prePago ? valor : BigDecimal.ZERO, prePago ? BigDecimal.ZERO : valor, BigDecimal.ZERO, true, agora);
    }

    /** Recria um cliente já existente a partir dos dados persistidos. */
    public static Cliente reconstituir(UUID id, String nome, Documento documento, TipoPlano tipoPlano,
                                       BigDecimal saldo, BigDecimal limiteMensal, BigDecimal consumoMes,
                                       boolean ativo, Instant criadoEm) {
        return new Cliente(id, nome, documento, tipoPlano, saldo, limiteMensal, consumoMes, ativo, criadoEm);
    }

    public void cobrar(BigDecimal valor) {
        if (!ativo) {
            throw new RegraDeNegocioException("Cliente inativo não pode enviar mensagens");
        }
        tipoPlano.plano().cobrar(this, dinheiro(valor));
    }

    public void estornar(BigDecimal valor) {
        tipoPlano.plano().estornar(this, dinheiro(valor));
    }

    public BigDecimal disponivel() {
        return tipoPlano.plano().disponivel(this);
    }

    public void adicionarCredito(BigDecimal valor) {
        exigirPlano(TipoPlano.PRE_PAGO, "Créditos só podem ser adicionados a clientes pré-pagos");
        saldo = saldo.add(positivo(valor, "Valor do crédito"));
    }

    public void ajustarLimite(BigDecimal novoLimite) {
        exigirPlano(TipoPlano.POS_PAGO, "O limite só pode ser ajustado em clientes pós-pagos");
        BigDecimal limite = naoNegativo(novoLimite, "Limite");
        if (limite.compareTo(consumoMes) < 0) {
            throw new RegraDeNegocioException(
                    "O limite não pode ser menor que o consumo do mês (R$ %s)".formatted(consumoMes));
        }
        limiteMensal = limite;
    }

    /**
     * Converte o cliente de plano. No pré → pós, o saldo restante vira crédito no consumo do mês.
     * No pós → pré, não pode haver consumo pendente, e um eventual crédito vira saldo.
     */
    public void converterPara(TipoPlano novoPlano, BigDecimal limiteMensalPosPago) {
        if (novoPlano == tipoPlano) {
            throw new RegraDeNegocioException("O cliente já está no plano " + novoPlano);
        }
        if (novoPlano == TipoPlano.POS_PAGO) {
            limiteMensal = positivo(limiteMensalPosPago, "Limite mensal");
            consumoMes = saldo.negate();
            saldo = BigDecimal.ZERO.setScale(2);
        } else {
            if (consumoMes.signum() > 0) {
                throw new RegraDeNegocioException(
                        "Há consumo pendente de R$ %s no mês; quite-o antes de converter".formatted(consumoMes));
            }
            saldo = consumoMes.negate();
            consumoMes = BigDecimal.ZERO.setScale(2);
            limiteMensal = BigDecimal.ZERO.setScale(2);
        }
        tipoPlano = novoPlano;
    }

    public void renomear(String novoNome) {
        nome = nomeValido(novoNome);
    }

    public void ativar() {
        ativo = true;
    }

    public void inativar() {
        ativo = false;
    }

    void alterarSaldo(BigDecimal novoSaldo) {
        saldo = dinheiro(novoSaldo);
    }

    void alterarConsumoMes(BigDecimal novoConsumo) {
        consumoMes = dinheiro(novoConsumo);
    }

    private void exigirPlano(TipoPlano esperado, String mensagem) {
        if (tipoPlano != esperado) {
            throw new RegraDeNegocioException(mensagem);
        }
    }

    private static String nomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome é obrigatório");
        }
        if (nome.strip().length() > 120) {
            throw new RegraDeNegocioException("Nome deve ter no máximo 120 caracteres");
        }
        return nome.strip();
    }

    private static BigDecimal positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException(campo + " deve ser maior que zero");
        }
        return dinheiro(valor);
    }

    private static BigDecimal naoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraDeNegocioException(campo + " não pode ser negativo");
        }
        return dinheiro(valor);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return Objects.requireNonNull(valor).setScale(2, RoundingMode.HALF_EVEN);
    }

    public UUID id() { return id; }
    public String nome() { return nome; }
    public Documento documento() { return documento; }
    public TipoPlano tipoPlano() { return tipoPlano; }
    public BigDecimal saldo() { return saldo; }
    public BigDecimal limiteMensal() { return limiteMensal; }
    public BigDecimal consumoMes() { return consumoMes; }
    public boolean ativo() { return ativo; }
    public Instant criadoEm() { return criadoEm; }
}

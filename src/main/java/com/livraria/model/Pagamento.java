package com.livraria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de cada tentativa de pagamento de um pedido (aprovada ou recusada). */
@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento forma;

    @Column(nullable = false)
    private int parcelas;

    @Column(name = "final_cartao", length = 4)
    private String finalCartao; // só os 4 últimos dígitos; nulo no Pix

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false)
    private LocalDateTime data = LocalDateTime.now();

    protected Pagamento() { }

    public Pagamento(Pedido pedido, BigDecimal valor, FormaPagamento forma, int parcelas,
                     String finalCartao, StatusPagamento status) {
        this.pedido = pedido;
        this.valor = valor;
        this.forma = forma;
        this.parcelas = parcelas;
        this.finalCartao = finalCartao;
        this.status = status;
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public BigDecimal getValor() { return valor; }
    public FormaPagamento getForma() { return forma; }
    public int getParcelas() { return parcelas; }
    public String getFinalCartao() { return finalCartao; }
    public StatusPagamento getStatus() { return status; }
    public LocalDateTime getData() { return data; }
}

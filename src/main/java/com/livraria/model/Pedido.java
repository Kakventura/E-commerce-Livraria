package com.livraria.model;

import com.livraria.exception.DadosInvalidosException;
import com.livraria.exception.PedidoSemItensException;
import com.livraria.exception.QuantidadeInvalidaException;
import com.livraria.exception.TransicaoInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    public static final int MAX_TENTATIVAS = 3;          // tentativas de pagamento recusadas
    public static final int MAX_PARCELAS = 3;            // RN04: até 3x sem juros
    public static final BigDecimal DESCONTO_CARTAO_A_VISTA = new BigDecimal("0.03"); // RN04
    public static final BigDecimal DESCONTO_PIX = new BigDecimal("0.08");            // RN04

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Embedded
    private Endereco enderecoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.EM_PROCESSAMENTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private int parcelas = 1;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal frete = BigDecimal.ZERO.setScale(2);

    @Column(name = "tentativas_recusadas", nullable = false)
    private int tentativasRecusadas = 0;

    protected Pedido() { }

    public Pedido(Cliente cliente, Endereco enderecoEntrega) {
        this.cliente = cliente;
        this.enderecoEntrega = enderecoEntrega;
    }

    // ---- itens e preços ----
    public void adicionarItem(Livro livro, int quantidade) {
        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException("A quantidade deve ser maior que zero.");
        }
        // RN02: o preço do item já é o preço final do formato, congelado no pedido
        itens.add(new ItemPedido(this, livro, quantidade, livro.getPrecoFinal()));
    }

    public int quantidadeDoLivro(String isbn) {
        return itens.stream()
                .filter(i -> i.getLivro().getIsbn().equals(isbn))
                .mapToInt(ItemPedido::getQuantidade)
                .sum();
    }

    public BigDecimal calcularSubtotal() {
        return itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void definirFrete(BigDecimal valor) {
        this.frete = valor;
    }

    // ---- pagamento (RN04) ----
    public void definirPagamento(FormaPagamento forma, int parcelas) {
        if (forma == null) {
            throw new DadosInvalidosException("Informe a forma de pagamento.");
        }
        if (parcelas < 1 || parcelas > MAX_PARCELAS) {
            throw new DadosInvalidosException("Parcelamento permitido: de 1 a " + MAX_PARCELAS + " vezes.");
        }
        if (forma == FormaPagamento.PIX && parcelas != 1) {
            throw new DadosInvalidosException("O pagamento via Pix deve ser à vista.");
        }
        this.formaPagamento = forma;
        this.parcelas = parcelas;
    }

    /** RN04: Pix à vista 8%; cartão à vista 3%; cartão parcelado sem desconto. */
    public BigDecimal descontoPagamento() {
        if (formaPagamento == FormaPagamento.PIX) return DESCONTO_PIX;
        if (formaPagamento == FormaPagamento.CARTAO && parcelas == 1) return DESCONTO_CARTAO_A_VISTA;
        return BigDecimal.ZERO;
    }

    /** Valor dos livros após o desconto do pagamento (o frete não entra no desconto). */
    public BigDecimal calcularValorProdutos() {
        BigDecimal sub = calcularSubtotal();
        return sub.subtract(sub.multiply(descontoPagamento())).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularDesconto() {
        return calcularSubtotal().subtract(calcularValorProdutos());
    }

    public BigDecimal calcularTotal() {
        if (itens.isEmpty()) {
            throw new PedidoSemItensException("O pedido deve conter ao menos um item.");
        }
        return calcularValorProdutos().add(frete);
    }

    /** Registra pagamento recusado; ao esgotar as tentativas o pedido é cancelado. */
    public boolean registrarRecusa() {
        tentativasRecusadas++;
        if (tentativasRecusadas >= MAX_TENTATIVAS) {
            cancelar();
            return true;
        }
        return false;
    }

    public int tentativasRestantes() {
        return Math.max(0, MAX_TENTATIVAS - tentativasRecusadas);
    }

    // ---- máquina de estados (RN05; base do Diagrama de Estados / Grafo de Estados) ----
    public void confirmarPagamento() { transitar(StatusPedido.EM_PROCESSAMENTO, StatusPedido.CONFIRMADO); }
    public void cancelar()           { transitar(StatusPedido.EM_PROCESSAMENTO, StatusPedido.CANCELADO); }
    public void enviar()             { transitar(StatusPedido.CONFIRMADO, StatusPedido.EM_TRANSPORTE); }
    public void receber()            { transitar(StatusPedido.EM_TRANSPORTE, StatusPedido.FINALIZADO); }

    private void transitar(StatusPedido origem, StatusPedido destino) {
        if (status != origem) {
            throw new TransicaoInvalidaException("Não é possível ir de " + status + " para " + destino + ".");
        }
        status = destino;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Endereco getEnderecoEntrega() { return enderecoEntrega; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public StatusPedido getStatus() { return status; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public int getParcelas() { return parcelas; }
    public BigDecimal getFrete() { return frete; }
}

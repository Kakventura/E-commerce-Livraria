package com.livraria.model;

import com.livraria.exception.DadosInvalidosException;
import com.livraria.exception.EstoqueInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "livros")
public class Livro {
    /** RN03: ao atingir este estoque, o funcionário é notificado. */
    public static final int ESTOQUE_MINIMO = 2;

    @Id
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoLivro formato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLivro status;

    protected Livro() { }

    public Livro(String isbn, String titulo, BigDecimal preco, int estoque,
                 FormatoLivro formato, StatusLivro status) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.preco = preco;
        this.estoque = estoque;
        this.formato = formato;
        this.status = status;
    }

    public boolean disponivel() {
        return status == StatusLivro.DISPONIVEL;
    }

    /** RN02: preço calculado conforme o formato. */
    public BigDecimal getPrecoFinal() {
        return formato.precoFinal(preco);
    }

    public boolean temEstoque(int quantidade) {
        return estoque >= quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (!temEstoque(quantidade)) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para '" + titulo
                    + "': disponível " + estoque + ", solicitado " + quantidade + ".");
        }
        estoque -= quantidade;
        if (estoque == 0 && status == StatusLivro.DISPONIVEL) {
            status = StatusLivro.INDISPONIVEL; // RN01
        }
    }

    public void reporEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade de reposição deve ser maior que zero.");
        }
        estoque += quantidade;
        if (status == StatusLivro.INDISPONIVEL) {
            status = StatusLivro.DISPONIVEL; // RN01
        }
    }

    public void alterarStatus(StatusLivro novo) {
        if (novo == null) throw new DadosInvalidosException("Informe o status do livro.");
        this.status = novo;
    }

    /** RN03: só livros em circulação geram notificação de estoque mínimo. */
    public boolean atingiuEstoqueMinimo() {
        return status != StatusLivro.FORA_DE_CIRCULACAO && estoque <= ESTOQUE_MINIMO;
    }

    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public BigDecimal getPreco() { return preco; }
    public int getEstoque() { return estoque; }
    public FormatoLivro getFormato() { return formato; }
    public StatusLivro getStatus() { return status; }
}

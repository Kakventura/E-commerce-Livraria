package com.livraria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** RN03: mensagem destinada ao funcionário (estoque mínimo atingido). */
@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime data = LocalDateTime.now();

    protected Notificacao() { }

    public Notificacao(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getId() { return id; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getData() { return data; }
}

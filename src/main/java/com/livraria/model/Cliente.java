package com.livraria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Embedded
    private Endereco enderecoPadrao; // opcional

    protected Cliente() { }

    public Cliente(String nome, String email, String senhaHash, Endereco enderecoPadrao) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.enderecoPadrao = enderecoPadrao;
    }

    public boolean confereSenha(String hash) {
        return senhaHash.equals(hash);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Endereco getEnderecoPadrao() { return enderecoPadrao; }
}

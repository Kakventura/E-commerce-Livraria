package com.livraria.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Endereco(String logradouro, String numero, String cidade, String uf, String cep) {
}

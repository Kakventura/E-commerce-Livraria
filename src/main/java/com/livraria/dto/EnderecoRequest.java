package com.livraria.dto;

import com.livraria.model.Endereco;

public record EnderecoRequest(String logradouro, String numero, String cidade, String uf, String cep) {
    public Endereco paraModel() {
        return new Endereco(logradouro, numero, cidade, uf, cep);
    }
}

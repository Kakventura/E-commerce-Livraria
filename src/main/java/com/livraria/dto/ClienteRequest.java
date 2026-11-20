package com.livraria.dto;

public record ClienteRequest(String nome, String email, String senha, EnderecoRequest enderecoPadrao) {
}

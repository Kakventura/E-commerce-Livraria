package com.livraria.dto;

public record LoginResponse(String token, Long clienteId, String nome) {
}

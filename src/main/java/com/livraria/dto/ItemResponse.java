package com.livraria.dto;

import java.math.BigDecimal;

public record ItemResponse(String isbn, String titulo, int quantidade,
                           BigDecimal precoUnitario, BigDecimal subtotal) {
}

package com.livraria.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** RN02: formatos do livro e desconto sobre o preço (0%, 5% e 10%). */
public enum FormatoLivro {
    CAPA_DURA("0.00"), BROCHURA("0.05"), DIGITAL("0.10");

    private final BigDecimal desconto;

    FormatoLivro(String desconto) {
        this.desconto = new BigDecimal(desconto);
    }

    public BigDecimal precoFinal(BigDecimal preco) {
        return preco.subtract(preco.multiply(desconto)).setScale(2, RoundingMode.HALF_UP);
    }
}

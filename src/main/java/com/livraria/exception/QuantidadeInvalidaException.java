package com.livraria.exception;

/** Quantidade deve ser maior que zero (HTTP 400). */
public class QuantidadeInvalidaException extends DominioException {
    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}

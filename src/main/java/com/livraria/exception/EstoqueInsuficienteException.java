package com.livraria.exception;

/** RN02: estoque deve cobrir a quantidade solicitada (HTTP 409). */
public class EstoqueInsuficienteException extends DominioException {
    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}

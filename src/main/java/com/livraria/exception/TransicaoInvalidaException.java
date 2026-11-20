package com.livraria.exception;

/** Transição de estado não permitida do Pedido (HTTP 409). */
public class TransicaoInvalidaException extends DominioException {
    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}

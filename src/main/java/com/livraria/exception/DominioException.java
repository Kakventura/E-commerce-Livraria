package com.livraria.exception;

/** Erro base das regras de negócio. */
public abstract class DominioException extends RuntimeException {
    protected DominioException(String mensagem) {
        super(mensagem);
    }
}

package com.livraria.exception;

/** Fluxo de exceção: cliente inexistente (HTTP 404). */
public class ClienteNaoEncontradoException extends DominioException {
    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

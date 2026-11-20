package com.livraria.exception;

/** Pedido inexistente (HTTP 404). */
public class PedidoNaoEncontradoException extends DominioException {
    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

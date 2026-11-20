package com.livraria.exception;

/** RN04: o pedido deve ter ao menos um item (HTTP 400). */
public class PedidoSemItensException extends DominioException {
    public PedidoSemItensException(String mensagem) {
        super(mensagem);
    }
}

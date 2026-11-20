package com.livraria.exception;

/** Pagamento recusado (HTTP 422). Traz os dados para o cliente tentar de novo. */
public class PagamentoRecusadoException extends DominioException {
    private final Long pedidoId;
    private final int tentativasRestantes;
    private final boolean pedidoCancelado;

    public PagamentoRecusadoException(String mensagem, Long pedidoId, int tentativasRestantes, boolean pedidoCancelado) {
        super(mensagem);
        this.pedidoId = pedidoId;
        this.tentativasRestantes = tentativasRestantes;
        this.pedidoCancelado = pedidoCancelado;
    }

    public Long getPedidoId() { return pedidoId; }
    public int getTentativasRestantes() { return tentativasRestantes; }
    public boolean isPedidoCancelado() { return pedidoCancelado; }
}

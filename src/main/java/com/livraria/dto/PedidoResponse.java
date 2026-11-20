package com.livraria.dto;

import com.livraria.model.Pedido;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(Long pedidoId, Long clienteId, String status, List<ItemResponse> itens,
                             BigDecimal subtotal, BigDecimal desconto, BigDecimal frete, BigDecimal total,
                             String formaPagamento, int parcelas, int tentativasRestantes) {

    public static PedidoResponse de(Pedido p) {
        List<ItemResponse> itens = p.getItens().stream()
                .map(i -> new ItemResponse(i.getLivro().getIsbn(), i.getLivro().getTitulo(),
                        i.getQuantidade(), i.getPrecoUnitario(), i.subtotal()))
                .toList();
        return new PedidoResponse(p.getId(), p.getCliente().getId(), p.getStatus().name(), itens,
                p.calcularSubtotal(), p.calcularDesconto(), p.getFrete(), p.calcularTotal(),
                p.getFormaPagamento() == null ? null : p.getFormaPagamento().name(),
                p.getParcelas(), p.tentativasRestantes());
    }
}

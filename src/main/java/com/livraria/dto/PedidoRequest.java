package com.livraria.dto;

import java.util.List;

/** O cliente vem do token de autenticação. enderecoEntrega é opcional (usa o endereço padrão). */
public record PedidoRequest(List<ItemRequest> itens, EnderecoRequest enderecoEntrega, PagamentoRequest pagamento) {
}

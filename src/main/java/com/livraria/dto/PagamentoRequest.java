package com.livraria.dto;

import com.livraria.model.FormaPagamento;

/** parcelas é opcional (padrão 1). numeroCartao só é usado no cartão. */
public record PagamentoRequest(FormaPagamento forma, String numeroCartao, String titular, Integer parcelas) {
    public int parcelasOuUma() {
        return parcelas == null ? 1 : parcelas;
    }
}

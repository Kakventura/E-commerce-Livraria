package com.livraria.gateway;

import com.livraria.model.FormaPagamento;

import java.math.BigDecimal;

/** Ator secundário de pagamento (Sistema de Cartão ou Sistema de Banco/Pix). */
public interface PagamentoGateway {
    FormaPagamento forma();

    boolean autorizar(BigDecimal valor, String numeroCartao);
}

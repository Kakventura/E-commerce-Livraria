package com.livraria.gateway;

import com.livraria.model.FormaPagamento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Sistema de Banco (Pix). Não há contrato com um banco real: a cobrança Pix é aprovada
 * para qualquer valor positivo. Para integrar um banco, basta outra implementação de PagamentoGateway.
 */
@Component
public class PagamentoGatewayPix implements PagamentoGateway {

    @Override
    public FormaPagamento forma() {
        return FormaPagamento.PIX;
    }

    @Override
    public boolean autorizar(BigDecimal valor, String numeroCartao) {
        return valor != null && valor.signum() > 0;
    }
}

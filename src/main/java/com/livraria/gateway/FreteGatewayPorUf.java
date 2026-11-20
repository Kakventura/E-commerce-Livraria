package com.livraria.gateway;

import com.livraria.model.Endereco;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Sistema de Frete simulado (sem contrato com transportadora): tabela por UF.
 * SP = R$ 15,00; demais estados = R$ 30,00. Para integrar uma transportadora, troque a implementação.
 */
@Component
public class FreteGatewayPorUf implements FreteGateway {

    @Override
    public BigDecimal calcular(Endereco destino, BigDecimal subtotal) {
        boolean sp = destino != null && "SP".equalsIgnoreCase(destino.uf());
        return new BigDecimal(sp ? "15.00" : "30.00");
    }
}

package com.livraria.gateway;

import com.livraria.model.Endereco;

import java.math.BigDecimal;

/** Ator secundário: Sistema de Frete. */
public interface FreteGateway {
    BigDecimal calcular(Endereco destino, BigDecimal subtotal);
}

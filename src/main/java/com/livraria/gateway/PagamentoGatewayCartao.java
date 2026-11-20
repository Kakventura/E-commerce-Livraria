package com.livraria.gateway;

import com.livraria.model.FormaPagamento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Autorização do pagamento por cartão.
 *
 * Não há contrato com uma operadora real, então a autorização é feita por regra interna:
 * o número do cartão precisa ter 13 a 19 dígitos e passar no algoritmo de Luhn (o mesmo
 * usado pelas bandeiras para validar o número). Cartão inválido => pagamento RECUSADO.
 * Para integrar uma operadora real, basta criar outra implementação de PagamentoGateway.
 */
@Component
public class PagamentoGatewayCartao implements PagamentoGateway {

    @Override
    public FormaPagamento forma() {
        return FormaPagamento.CARTAO;
    }

    @Override
    public boolean autorizar(BigDecimal valor, String numeroCartao) {
        if (valor == null || valor.signum() <= 0 || numeroCartao == null) {
            return false;
        }
        String digitos = numeroCartao.replaceAll("[\\s-]", "");
        return digitos.matches("\\d{13,19}") && passaNoLuhn(digitos);
    }

    private boolean passaNoLuhn(String digitos) {
        int soma = 0;
        boolean dobrar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (dobrar) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            soma += d;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }
}

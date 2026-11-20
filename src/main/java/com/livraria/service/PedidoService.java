package com.livraria.service;

import com.livraria.dto.PagamentoRequest;
import com.livraria.dto.PedidoRequest;
import com.livraria.dto.PedidoResponse;

import java.util.List;

public interface PedidoService {
    /** Caso de uso: Efetuar Pedido (cria o pedido EM_PROCESSAMENTO e tenta o pagamento). */
    PedidoResponse efetuarPedido(Long clienteId, PedidoRequest request);

    /** Nova tentativa de pagamento de pedido EM_PROCESSAMENTO (após recusa). */
    PedidoResponse pagar(Long clienteId, Long pedidoId, PagamentoRequest pagamento);

    /** EM_PROCESSAMENTO -> CANCELADO (cliente desiste). */
    PedidoResponse cancelar(Long clienteId, Long pedidoId);

    /** Visualizar Pedido (CSU04). */
    PedidoResponse buscar(Long clienteId, Long pedidoId);

    List<PedidoResponse> listar(Long clienteId);

    /** CONFIRMADO -> EM_TRANSPORTE. */
    PedidoResponse enviar(Long pedidoId);

    /** EM_TRANSPORTE -> FINALIZADO. */
    PedidoResponse receber(Long pedidoId);
}

package com.livraria.controller;

import com.livraria.dto.PagamentoRequest;
import com.livraria.dto.PedidoRequest;
import com.livraria.dto.PedidoResponse;
import com.livraria.service.AuthService;
import com.livraria.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private static final String AUTH = "Authorization";

    private final PedidoService service;
    private final AuthService auth;

    public PedidoController(PedidoService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse efetuarPedido(@RequestHeader(value = AUTH, required = false) String token,
                                        @RequestBody PedidoRequest request) {
        return service.efetuarPedido(auth.clienteId(token), request);
    }

    @GetMapping
    public List<PedidoResponse> listar(@RequestHeader(value = AUTH, required = false) String token) {
        return service.listar(auth.clienteId(token));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@RequestHeader(value = AUTH, required = false) String token,
                                 @PathVariable Long id) {
        return service.buscar(auth.clienteId(token), id);
    }

    @PostMapping("/{id}/pagamento")
    public PedidoResponse pagar(@RequestHeader(value = AUTH, required = false) String token,
                                @PathVariable Long id, @RequestBody PagamentoRequest request) {
        return service.pagar(auth.clienteId(token), id, request);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@RequestHeader(value = AUTH, required = false) String token,
                                   @PathVariable Long id) {
        return service.cancelar(auth.clienteId(token), id);
    }

    @PatchMapping("/{id}/enviar")
    public PedidoResponse enviar(@PathVariable Long id) {
        return service.enviar(id);
    }

    @PatchMapping("/{id}/receber")
    public PedidoResponse receber(@PathVariable Long id) {
        return service.receber(id);
    }
}

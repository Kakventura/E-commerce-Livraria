package com.livraria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traduz exceções de domínio em respostas HTTP (sem lógica de erro nos controllers). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<Map<String, Object>> tratar(DominioException ex) {
        HttpStatus status = statusDe(ex);
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("erro", ex.getClass().getSimpleName());
        corpo.put("mensagem", ex.getMessage());
        if (ex instanceof PagamentoRecusadoException p) {
            corpo.put("pedidoId", p.getPedidoId());
            corpo.put("tentativasRestantes", p.getTentativasRestantes());
            corpo.put("pedidoCancelado", p.isPedidoCancelado());
        }
        return ResponseEntity.status(status).body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> corpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "erro", "CorpoInvalido",
                "mensagem", "Corpo da requisição ausente ou em formato inválido."));
    }

    private HttpStatus statusDe(DominioException ex) {
        if (ex instanceof NaoAutenticadoException) return HttpStatus.UNAUTHORIZED;
        if (ex instanceof ClienteNaoEncontradoException
                || ex instanceof LivroNaoEncontradoException
                || ex instanceof PedidoNaoEncontradoException) return HttpStatus.NOT_FOUND;
        if (ex instanceof EstoqueInsuficienteException
                || ex instanceof LivroIndisponivelException
                || ex instanceof RegistroDuplicadoException
                || ex instanceof TransicaoInvalidaException) return HttpStatus.CONFLICT;
        if (ex instanceof PagamentoRecusadoException) return HttpStatus.UNPROCESSABLE_ENTITY;
        return HttpStatus.BAD_REQUEST;
    }
}

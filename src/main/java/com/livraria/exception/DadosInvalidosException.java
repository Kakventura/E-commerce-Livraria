package com.livraria.exception;

/** Dados de entrada inválidos (HTTP 400). */
public class DadosInvalidosException extends DominioException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}

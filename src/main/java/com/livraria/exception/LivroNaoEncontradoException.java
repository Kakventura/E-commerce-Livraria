package com.livraria.exception;

/** Fluxo de exceção: livro inexistente (HTTP 404). */
public class LivroNaoEncontradoException extends DominioException {
    public LivroNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

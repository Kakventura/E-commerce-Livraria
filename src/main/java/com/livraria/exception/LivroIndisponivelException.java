package com.livraria.exception;

/** RN01: livro indisponível ou fora de circulação (HTTP 409). */
public class LivroIndisponivelException extends DominioException {
    public LivroIndisponivelException(String mensagem) {
        super(mensagem);
    }
}

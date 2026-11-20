package com.livraria.exception;

/** Sem endereço informado e sem endereço padrão (HTTP 400). */
public class EnderecoNaoInformadoException extends DominioException {
    public EnderecoNaoInformadoException(String mensagem) {
        super(mensagem);
    }
}

package com.livraria.exception;

/** Registro já cadastrado, ex.: ISBN ou e-mail repetido (HTTP 409). */
public class RegistroDuplicadoException extends DominioException {
    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}

package com.livraria.exception;

/** Cliente sem sessão válida (HTTP 401). */
public class NaoAutenticadoException extends DominioException {
    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}

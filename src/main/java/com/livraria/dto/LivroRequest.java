package com.livraria.dto;

import com.livraria.model.FormatoLivro;
import com.livraria.model.StatusLivro;

import java.math.BigDecimal;

/** formato e status são opcionais: padrão CAPA_DURA e DISPONIVEL. */
public record LivroRequest(String isbn, String titulo, BigDecimal preco, Integer estoque,
                           FormatoLivro formato, StatusLivro status) {
}

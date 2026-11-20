package com.livraria.controller;

import com.livraria.dto.EstoqueRequest;
import com.livraria.dto.StatusLivroRequest;
import com.livraria.dto.LivroRequest;
import com.livraria.model.Livro;
import com.livraria.service.LivroService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Livro> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Livro cadastrar(@RequestBody LivroRequest request) {
        return service.cadastrar(request);
    }

    /** Repõe estoque: soma a quantidade informada ao estoque atual. */
    @PatchMapping("/{isbn}/estoque")
    public Livro reporEstoque(@PathVariable String isbn, @RequestBody EstoqueRequest request) {
        return service.reporEstoque(isbn, request.quantidade());
    }

    /** RN01: altera o status (DISPONIVEL, INDISPONIVEL, FORA_DE_CIRCULACAO). */
    @PatchMapping("/{isbn}/status")
    public Livro alterarStatus(@PathVariable String isbn, @RequestBody StatusLivroRequest request) {
        return service.alterarStatus(isbn, request.status());
    }
}

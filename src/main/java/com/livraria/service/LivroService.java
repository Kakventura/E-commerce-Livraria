package com.livraria.service;

import com.livraria.dto.LivroRequest;
import com.livraria.exception.DadosInvalidosException;
import com.livraria.exception.LivroNaoEncontradoException;
import com.livraria.exception.RegistroDuplicadoException;
import com.livraria.model.FormatoLivro;
import com.livraria.model.Livro;
import com.livraria.model.StatusLivro;
import com.livraria.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livros;

    public LivroService(LivroRepository livros) {
        this.livros = livros;
    }

    @Transactional
    public Livro cadastrar(LivroRequest req) {
        if (req.isbn() == null || req.isbn().isBlank()) throw new DadosInvalidosException("Informe o ISBN.");
        if (req.titulo() == null || req.titulo().isBlank()) throw new DadosInvalidosException("Informe o título.");
        if (req.preco() == null || req.preco().signum() <= 0) throw new DadosInvalidosException("O preço deve ser maior que zero.");
        if (req.estoque() == null || req.estoque() < 0) throw new DadosInvalidosException("O estoque não pode ser negativo.");
        if (livros.existsById(req.isbn().trim())) throw new RegistroDuplicadoException("ISBN " + req.isbn() + " já cadastrado.");
        return livros.save(new Livro(req.isbn().trim(), req.titulo().trim(), req.preco(), req.estoque(),
                req.formato() != null ? req.formato() : FormatoLivro.CAPA_DURA,
                req.status() != null ? req.status() : StatusLivro.DISPONIVEL));
    }

    @Transactional(readOnly = true)
    public List<Livro> listar() {
        return livros.findAll();
    }

    @Transactional
    public Livro reporEstoque(String isbn, int quantidade) {
        Livro livro = obter(isbn);
        livro.reporEstoque(quantidade);
        return livro;
    }

    @Transactional
    public Livro alterarStatus(String isbn, StatusLivro status) {
        Livro livro = obter(isbn);
        livro.alterarStatus(status);
        return livro;
    }

    private Livro obter(String isbn) {
        return livros.findById(isbn)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro " + isbn + " não encontrado."));
    }
}

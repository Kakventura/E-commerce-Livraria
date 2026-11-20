package com.livraria.service;

import com.livraria.dto.ClienteRequest;
import com.livraria.exception.DadosInvalidosException;
import com.livraria.exception.RegistroDuplicadoException;
import com.livraria.model.Cliente;
import com.livraria.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional
    public Cliente cadastrar(ClienteRequest req) {
        if (req.nome() == null || req.nome().isBlank()) throw new DadosInvalidosException("Informe o nome.");
        if (req.email() == null || !req.email().contains("@")) throw new DadosInvalidosException("Informe um e-mail válido.");
        if (req.senha() == null || req.senha().length() < 4) throw new DadosInvalidosException("Informe uma senha de ao menos 4 caracteres.");
        if (clientes.existsByEmail(req.email().trim())) throw new RegistroDuplicadoException("E-mail já cadastrado.");
        return clientes.save(new Cliente(req.nome().trim(), req.email().trim(), Senhas.hash(req.senha()),
                req.enderecoPadrao() != null ? req.enderecoPadrao().paraModel() : null));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.findAll();
    }
}

package com.livraria.service;

import com.livraria.dto.LoginRequest;
import com.livraria.dto.LoginResponse;
import com.livraria.exception.NaoAutenticadoException;
import com.livraria.model.Cliente;
import com.livraria.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Autenticação do cliente: login por e-mail/senha e sessão por token (Bearer). */
@Service
public class AuthService {

    private final ClienteRepository clientes;
    private final Map<String, Long> sessoes = new ConcurrentHashMap<>();

    public AuthService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        if (req == null || req.email() == null || req.senha() == null) {
            throw new NaoAutenticadoException("Informe e-mail e senha.");
        }
        Cliente c = clientes.findByEmail(req.email().trim())
                .filter(x -> x.confereSenha(Senhas.hash(req.senha())))
                .orElseThrow(() -> new NaoAutenticadoException("E-mail ou senha inválidos."));
        String token = UUID.randomUUID().toString();
        sessoes.put(token, c.getId());
        return new LoginResponse(token, c.getId(), c.getNome());
    }

    /** Recebe o cabeçalho "Authorization: Bearer <token>" e devolve o id do cliente. */
    public Long clienteId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new NaoAutenticadoException("Cliente não autenticado. Faça login.");
        }
        Long id = sessoes.get(authorization.substring(7).trim());
        if (id == null) {
            throw new NaoAutenticadoException("Sessão inválida ou expirada. Faça login novamente.");
        }
        return id;
    }
}

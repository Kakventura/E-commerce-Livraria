package com.livraria.service;

import com.livraria.model.Notificacao;
import com.livraria.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Mensagens para o funcionário (RN03). */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacoes;

    public NotificacaoService(NotificacaoRepository notificacoes) {
        this.notificacoes = notificacoes;
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar() {
        return notificacoes.findAllByOrderByIdDesc();
    }
}

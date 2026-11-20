package com.livraria;

import com.livraria.dto.ItemRequest;
import com.livraria.dto.PagamentoRequest;
import com.livraria.dto.PedidoRequest;
import com.livraria.dto.PedidoResponse;
import com.livraria.exception.EstoqueInsuficienteException;
import com.livraria.exception.LivroIndisponivelException;
import com.livraria.exception.NaoAutenticadoException;
import com.livraria.exception.PagamentoRecusadoException;
import com.livraria.exception.PedidoSemItensException;
import com.livraria.exception.TransicaoInvalidaException;
import com.livraria.model.Cliente;
import com.livraria.model.Endereco;
import com.livraria.model.FormaPagamento;
import com.livraria.model.FormatoLivro;
import com.livraria.model.Livro;
import com.livraria.model.StatusLivro;
import com.livraria.model.StatusPedido;
import com.livraria.repository.ClienteRepository;
import com.livraria.repository.LivroRepository;
import com.livraria.repository.NotificacaoRepository;
import com.livraria.repository.PedidoRepository;
import com.livraria.service.AuthService;
import com.livraria.service.PedidoService;
import com.livraria.service.Senhas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integração (service + JPA + H2). Cada método corresponde a um caso de teste
 * (#01 a #14) da planilha "Realizar Pedido". Valores: livro de R$ 100,00 (sem frete).
 */
@SpringBootTest
@Transactional
class PedidoServiceTest {

    private static final PagamentoRequest CARTAO_1X = new PagamentoRequest(FormaPagamento.CARTAO, "4111111111111111", "Ana", 1);
    private static final PagamentoRequest CARTAO_3X = new PagamentoRequest(FormaPagamento.CARTAO, "4111111111111111", "Ana", 3);
    private static final PagamentoRequest CARTAO_RUIM = new PagamentoRequest(FormaPagamento.CARTAO, "4111111111111112", "Ana", 1);
    private static final PagamentoRequest PIX = new PagamentoRequest(FormaPagamento.PIX, null, "Ana", 1);

    @Autowired PedidoService service;
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired LivroRepository livros;
    @Autowired PedidoRepository pedidos;
    @Autowired NotificacaoRepository notificacoes;

    Cliente ana;

    @BeforeEach
    void setUp() {
        ana = clientes.save(new Cliente("Ana", "ana@email.com", Senhas.hash("1234"),
                new Endereco("Rua das Flores", "100", "São Paulo", "SP", "01000-000")));
        livros.save(new Livro("DURA", "Capa dura", bd("100"), 10, FormatoLivro.CAPA_DURA, StatusLivro.DISPONIVEL));
        livros.save(new Livro("BROC", "Brochura", bd("100"), 10, FormatoLivro.BROCHURA, StatusLivro.DISPONIVEL));
        livros.save(new Livro("DIGI", "Digital", bd("100"), 10, FormatoLivro.DIGITAL, StatusLivro.DISPONIVEL));
        livros.save(new Livro("POUC", "Poucos", bd("100"), 3, FormatoLivro.CAPA_DURA, StatusLivro.DISPONIVEL));
        livros.save(new Livro("UM", "Um só", bd("100"), 1, FormatoLivro.CAPA_DURA, StatusLivro.DISPONIVEL));
        livros.save(new Livro("INDI", "Indisponível", bd("100"), 5, FormatoLivro.CAPA_DURA, StatusLivro.INDISPONIVEL));
        livros.save(new Livro("FORA", "Fora de circulação", bd("100"), 5, FormatoLivro.CAPA_DURA, StatusLivro.FORA_DE_CIRCULACAO));
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private static List<ItemRequest> itens(String... isbns) {
        return java.util.Arrays.stream(isbns).map(i -> new ItemRequest(i, 1)).toList();
    }

    private PedidoResponse pedir(List<ItemRequest> itens, PagamentoRequest pg) {
        return service.efetuarPedido(ana.getId(), new PedidoRequest(itens, null, pg));
    }

    private BigDecimal semFrete(PedidoResponse r) { return r.total().subtract(r.frete()); }

    private int estoque(String isbn) { return livros.findById(isbn).orElseThrow().getEstoque(); }

    private void ciclo(PedidoResponse r) {
        assertEquals("EM_TRANSPORTE", service.enviar(r.pedidoId()).status());
        assertEquals("FINALIZADO", service.receber(r.pedidoId()).status());
        assertEquals("FINALIZADO", service.buscar(ana.getId(), r.pedidoId()).status());
    }

    @Test void ct01FluxoPrincipal() {
        PedidoResponse r = pedir(itens("DURA"), CARTAO_1X);
        assertEquals("CONFIRMADO", r.status());
        assertEquals(bd("97.00"), semFrete(r));
        assertEquals(9, estoque("DURA"));
        ciclo(r);
    }

    @Test void ct02PagamentoRecusadoEDepoisAprovado() {
        var ex = assertThrows(PagamentoRecusadoException.class, () -> pedir(itens("DURA"), CARTAO_RUIM));
        assertEquals(2, ex.getTentativasRestantes());
        assertEquals(StatusPedido.EM_PROCESSAMENTO, pedidos.findById(ex.getPedidoId()).orElseThrow().getStatus());
        assertEquals(10, estoque("DURA"));
        PedidoResponse r = service.pagar(ana.getId(), ex.getPedidoId(), CARTAO_1X);
        assertEquals("CONFIRMADO", r.status());
        assertEquals(9, estoque("DURA"));
        ciclo(r);
    }

    @Test void ct03EstoqueMinimoNotificaFuncionario() {
        PedidoResponse r = pedir(itens("POUC"), CARTAO_1X);
        assertEquals(2, estoque("POUC"));
        assertEquals(1, notificacoes.count());
        ciclo(r);
    }

    @Test void ct04Pix() {
        PedidoResponse r = pedir(itens("DURA"), PIX);
        assertEquals(bd("92.00"), semFrete(r));
        ciclo(r);
    }

    @Test void ct05BrochuraCartaoEm3x() {
        PedidoResponse r = pedir(itens("BROC"), CARTAO_3X);
        assertEquals(bd("95.00"), semFrete(r));
        ciclo(r);
    }

    @Test void ct06BrochuraCartaoAVista() {
        assertEquals(bd("92.15"), semFrete(pedir(itens("BROC"), CARTAO_1X)));
    }

    @Test void ct07LivroDigital() {
        assertEquals(bd("87.30"), semFrete(pedir(itens("DIGI"), CARTAO_1X)));
    }

    @Test void ct08ClienteCancelaPedidoEmProcessamento() {
        var ex = assertThrows(PagamentoRecusadoException.class, () -> pedir(itens("DURA"), CARTAO_RUIM));
        assertEquals("CANCELADO", service.cancelar(ana.getId(), ex.getPedidoId()).status());
        assertEquals(10, estoque("DURA"));
        assertThrows(TransicaoInvalidaException.class, () -> service.cancelar(ana.getId(), ex.getPedidoId()));
    }

    @Test void ct09TentativasEsgotadasCancelaPedido() {
        var e1 = assertThrows(PagamentoRecusadoException.class, () -> pedir(itens("DURA"), CARTAO_RUIM));
        Long id = e1.getPedidoId();
        var e2 = assertThrows(PagamentoRecusadoException.class, () -> service.pagar(ana.getId(), id, CARTAO_RUIM));
        assertEquals(1, e2.getTentativasRestantes());
        var e3 = assertThrows(PagamentoRecusadoException.class, () -> service.pagar(ana.getId(), id, CARTAO_RUIM));
        assertTrue(e3.isPedidoCancelado());
        assertEquals(StatusPedido.CANCELADO, pedidos.findById(id).orElseThrow().getStatus());
        assertEquals(10, estoque("DURA"));
    }

    @Test void ct10LivroIndisponivelOuForaDeCirculacao() {
        assertThrows(LivroIndisponivelException.class, () -> pedir(itens("INDI"), CARTAO_1X));
        assertThrows(LivroIndisponivelException.class, () -> pedir(itens("FORA"), CARTAO_1X));
        assertEquals(0, pedidos.count());
    }

    @Test void ct11ClienteNaoAutenticado() {
        assertThrows(NaoAutenticadoException.class, () -> auth.clienteId(null));
        assertThrows(NaoAutenticadoException.class, () -> auth.clienteId("Bearer token-invalido"));
    }

    @Test void ct12CarrinhoVazio() {
        assertThrows(PedidoSemItensException.class, () -> pedir(List.of(), CARTAO_1X));
        assertEquals(0, pedidos.count());
    }

    @Test void ct13EstoqueInsuficiente() {
        assertThrows(EstoqueInsuficienteException.class,
                () -> pedir(List.of(new ItemRequest("UM", 2)), CARTAO_1X));
        assertEquals(0, pedidos.count());
        assertEquals(1, estoque("UM"));
    }

    @Test void ct14MaisDeUmItem() {
        PedidoResponse r = pedir(itens("DURA", "BROC"), CARTAO_1X);
        assertEquals(2, r.itens().size());
        assertEquals(bd("189.15"), semFrete(r));
        ciclo(r);
    }
}

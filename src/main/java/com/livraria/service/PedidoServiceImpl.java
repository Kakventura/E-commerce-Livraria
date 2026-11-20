package com.livraria.service;

import com.livraria.dto.ItemRequest;
import com.livraria.dto.PagamentoRequest;
import com.livraria.dto.PedidoRequest;
import com.livraria.dto.PedidoResponse;
import com.livraria.exception.DadosInvalidosException;
import com.livraria.exception.EnderecoNaoInformadoException;
import com.livraria.exception.EstoqueInsuficienteException;
import com.livraria.exception.LivroIndisponivelException;
import com.livraria.exception.LivroNaoEncontradoException;
import com.livraria.exception.NaoAutenticadoException;
import com.livraria.exception.PagamentoRecusadoException;
import com.livraria.exception.PedidoNaoEncontradoException;
import com.livraria.exception.PedidoSemItensException;
import com.livraria.exception.TransicaoInvalidaException;
import com.livraria.gateway.FreteGateway;
import com.livraria.gateway.PagamentoGateway;
import com.livraria.model.Cliente;
import com.livraria.model.Endereco;
import com.livraria.model.FormaPagamento;
import com.livraria.model.ItemPedido;
import com.livraria.model.Livro;
import com.livraria.model.Notificacao;
import com.livraria.model.Pagamento;
import com.livraria.model.Pedido;
import com.livraria.model.StatusPagamento;
import com.livraria.model.StatusPedido;
import com.livraria.repository.ClienteRepository;
import com.livraria.repository.LivroRepository;
import com.livraria.repository.NotificacaoRepository;
import com.livraria.repository.PagamentoRepository;
import com.livraria.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final ClienteRepository clientes;
    private final LivroRepository livros;
    private final PedidoRepository pedidos;
    private final PagamentoRepository pagamentos;
    private final NotificacaoRepository notificacoes;
    private final FreteGateway freteGateway;
    private final List<PagamentoGateway> gateways;

    public PedidoServiceImpl(ClienteRepository clientes, LivroRepository livros, PedidoRepository pedidos,
                             PagamentoRepository pagamentos, NotificacaoRepository notificacoes,
                             FreteGateway freteGateway, List<PagamentoGateway> gateways) {
        this.clientes = clientes;
        this.livros = livros;
        this.pedidos = pedidos;
        this.pagamentos = pagamentos;
        this.notificacoes = notificacoes;
        this.freteGateway = freteGateway;
        this.gateways = gateways;
    }

    /**
     * Qualquer exceção desfaz tudo (rollback), exceto pagamento recusado: nesse caso o pedido
     * (EM_PROCESSAMENTO ou CANCELADO) e a tentativa recusada precisam ficar registrados.
     */
    @Override
    @Transactional(noRollbackFor = PagamentoRecusadoException.class)
    public PedidoResponse efetuarPedido(Long clienteId, PedidoRequest req) {
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new NaoAutenticadoException("Cliente não encontrado. Faça login novamente."));

        if (req.itens() == null || req.itens().isEmpty()) {
            throw new PedidoSemItensException("O carrinho precisa ter ao menos 1 item.");
        }
        Endereco endereco = req.enderecoEntrega() != null
                ? req.enderecoEntrega().paraModel() : cliente.getEnderecoPadrao();
        if (endereco == null) {
            throw new EnderecoNaoInformadoException("Informe um endereço de entrega.");
        }
        if (req.pagamento() == null) {
            throw new DadosInvalidosException("Informe o pagamento.");
        }

        Pedido pedido = new Pedido(cliente, endereco);
        for (ItemRequest item : req.itens()) {
            if (item.isbn() == null || item.isbn().isBlank()) {
                throw new DadosInvalidosException("Informe o ISBN de todos os itens.");
            }
            Livro livro = livros.findById(item.isbn())
                    .orElseThrow(() -> new LivroNaoEncontradoException("Livro " + item.isbn() + " não encontrado."));
            // RN01: só livros disponíveis podem ser comprados
            if (!livro.disponivel()) {
                throw new LivroIndisponivelException("O livro '" + livro.getTitulo()
                        + "' está " + livro.getStatus() + " e não pode ser comprado.");
            }
            // estoque suficiente (considera itens repetidos do mesmo livro)
            int total = pedido.quantidadeDoLivro(livro.getIsbn()) + item.quantidade();
            if (!livro.temEstoque(total)) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para '" + livro.getTitulo() + "'.");
            }
            pedido.adicionarItem(livro, item.quantidade()); // RN02 (preço do formato)
        }

        pedido.definirFrete(freteGateway.calcular(endereco, pedido.calcularSubtotal())); // Sistema de Frete
        pedido.definirPagamento(req.pagamento().forma(), req.pagamento().parcelasOuUma()); // RN04
        pedidos.save(pedido); // nasce EM_PROCESSAMENTO e já recebe id
        return tentarPagamento(pedido, req.pagamento());
    }

    @Override
    @Transactional(noRollbackFor = PagamentoRecusadoException.class)
    public PedidoResponse pagar(Long clienteId, Long pedidoId, PagamentoRequest pagamento) {
        Pedido pedido = obterDoCliente(clienteId, pedidoId);
        if (pedido.getStatus() != StatusPedido.EM_PROCESSAMENTO) {
            throw new TransicaoInvalidaException("Só é possível pagar pedido EM_PROCESSAMENTO (atual: "
                    + pedido.getStatus() + ").");
        }
        if (pagamento == null) {
            throw new DadosInvalidosException("Informe o pagamento.");
        }
        pedido.definirPagamento(pagamento.forma(), pagamento.parcelasOuUma());
        return tentarPagamento(pedido, pagamento);
    }

    /** Autoriza no gateway da forma escolhida; aprovado confirma e baixa estoque; recusado conta a tentativa. */
    private PedidoResponse tentarPagamento(Pedido pedido, PagamentoRequest pg) {
        BigDecimal total = pedido.calcularTotal();
        FormaPagamento forma = pedido.getFormaPagamento();
        PagamentoGateway gateway = gateways.stream().filter(g -> g.forma() == forma).findFirst()
                .orElseThrow(() -> new DadosInvalidosException("Forma de pagamento não suportada."));

        boolean aprovado = gateway.autorizar(total, pg.numeroCartao());
        pagamentos.save(new Pagamento(pedido, total, forma, pedido.getParcelas(),
                forma == FormaPagamento.CARTAO ? ultimos4(pg.numeroCartao()) : null,
                aprovado ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO));

        if (!aprovado) {
            boolean cancelado = pedido.registrarRecusa();
            throw new PagamentoRecusadoException(cancelado
                    ? "Pagamento recusado. Tentativas esgotadas: pedido " + pedido.getId() + " CANCELADO."
                    : "Pagamento recusado. Pedido " + pedido.getId() + " segue EM_PROCESSAMENTO; restam "
                            + pedido.tentativasRestantes() + " tentativa(s).",
                    pedido.getId(), pedido.tentativasRestantes(), cancelado);
        }

        pedido.confirmarPagamento();
        // Estoque só é baixado após o pagamento aprovado (RN03: avisa o funcionário no estoque mínimo)
        for (ItemPedido item : pedido.getItens()) {
            Livro livro = item.getLivro();
            livro.baixarEstoque(item.getQuantidade());
            if (livro.atingiuEstoqueMinimo()) {
                notificacoes.save(new Notificacao("Estoque mínimo atingido: '" + livro.getTitulo()
                        + "' (ISBN " + livro.getIsbn() + ") restam " + livro.getEstoque() + " exemplar(es)."));
            }
        }
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse cancelar(Long clienteId, Long pedidoId) {
        Pedido pedido = obterDoCliente(clienteId, pedidoId);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long clienteId, Long pedidoId) {
        return PedidoResponse.de(obterDoCliente(clienteId, pedidoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Long clienteId) {
        return pedidos.findByClienteIdOrderByIdDesc(clienteId).stream().map(PedidoResponse::de).toList();
    }

    @Override
    @Transactional
    public PedidoResponse enviar(Long pedidoId) {
        Pedido pedido = obter(pedidoId);
        pedido.enviar();
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse receber(Long pedidoId) {
        Pedido pedido = obter(pedidoId);
        pedido.receber();
        return PedidoResponse.de(pedido);
    }

    private Pedido obter(Long id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido " + id + " não encontrado."));
    }

    private Pedido obterDoCliente(Long clienteId, Long id) {
        Pedido p = obter(id);
        if (!p.getCliente().getId().equals(clienteId)) {
            throw new PedidoNaoEncontradoException("Pedido " + id + " não encontrado.");
        }
        return p;
    }

    private String ultimos4(String cartao) {
        if (cartao == null) return null;
        String d = cartao.replaceAll("\\D", "");
        return d.length() <= 4 ? d : d.substring(d.length() - 4);
    }
}

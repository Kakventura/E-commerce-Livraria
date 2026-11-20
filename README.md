# Livraria E-commerce – Caso de Uso "Efetuar Pedido"

API REST em **Java 17 + Spring Boot 3** com persistência em **PostgreSQL** (Spring Data JPA / Hibernate) e tela web
em `index.html`. Camadas: `controller` → `service` → `repository` (JPA) / `gateway` (sistemas externos); entidades em `model`.
Sem dados mockados: tudo é gravado no banco. Frete, cartão e Pix são **sistemas externos simulados** atrás de interfaces
(`FreteGateway`, `PagamentoGateway`), trocáveis por integrações reais.

## Executar
```bash
docker compose down -v && docker compose up -d   # PostgreSQL (use down -v para recriar o esquema)
mvn spring-boot:run                              # http://localhost:8080
mvn test                                         # JUnit (H2 em memória, não precisa do PostgreSQL)
```

## Regras implementadas
| Regra | Onde |
|---|---|
| RN01 status do livro (disponível, indisponível, fora de circulação) | `Livro`, `PedidoServiceImpl` |
| RN02 formato: capa dura 0%, brochura 5%, digital 10% | `FormatoLivro`, `ItemPedido` |
| RN03 estoque mínimo 2 → notificação ao funcionário | `Livro.atingiuEstoqueMinimo`, `Notificacao` |
| RN04 cartão (à vista 3% off, até 3x sem juros) e Pix (à vista, 8% off) | `Pedido`, `PagamentoGateway*` |
| RN05 status: EM_PROCESSAMENTO, CONFIRMADO, EM_TRANSPORTE, FINALIZADO, CANCELADO | `Pedido` (máquina de estados) |

Descontos de pagamento incidem sobre o valor dos livros; o frete (SP R$ 15,00; demais UFs R$ 30,00) é somado depois.
Pagamento recusado mantém o pedido EM_PROCESSAMENTO; após 3 recusas ele é CANCELADO. O estoque só baixa com pagamento aprovado.

## Endpoints
| Método | Rota | Auth |
|---|---|---|
| POST | `/clientes` · GET `/clientes` | – |
| POST | `/auth/login` → token | – |
| GET/POST | `/livros` · PATCH `/livros/{isbn}/estoque` · PATCH `/livros/{isbn}/status` | – (funcionário) |
| GET | `/notificacoes` | – (funcionário) |
| POST | `/pedidos` (efetuar) · GET `/pedidos` · GET `/pedidos/{id}` | Bearer |
| POST | `/pedidos/{id}/pagamento` (nova tentativa) · PATCH `/pedidos/{id}/cancelar` | Bearer |
| PATCH | `/pedidos/{id}/enviar` · `/pedidos/{id}/receber` | – (logística) |

Pagamento: cartão `4111111111111111` aprovado; `4111111111111112` recusado (Luhn).

## Testes pela própria tela
Abra http://localhost:8080 → aba **Roteiro de testes**: os 14 casos da planilha (#01–#14) rodam contra a API real,
mostram OK/NOK e o detalhe de cada verificação; "Copiar Status" gera a coluna para colar na planilha.
Também dá para testar manualmente nas abas **Loja** e **Funcionário**.

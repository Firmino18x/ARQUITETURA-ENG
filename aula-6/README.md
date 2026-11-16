# Aula 6 – Módulo de estoque

Evolução do projeto MVC da aula 5 com **status do produto** e **entrada/saída de estoque**, seguindo o mesmo padrão do fluxo de produto (`Controller → Service → Repository`, entidade JPA e view Thymeleaf).

## Regras implementadas

| Regra | Onde está |
|---|---|
| Produto tem status `DISPONIVEL`, `INDISPONIVEL` ou `DESCONTINUADO` | `model/enums/ProductStatus` |
| Produto novo começa `INDISPONIVEL` e com estoque 0 | `Product.prepareForCreation()` chamado em `ProductService.create` |
| Só o ADM pode descontinuar | `ProductService.discontinue(id, role)` |
| Produto descontinuado não pode ser movimentado no estoque | `Product.addStock / removeStock` |
| Saldo sai de zero → `DISPONIVEL` | `Product.addStock` |
| Saldo zera → `INDISPONIVEL` | `Product.removeStock` |
| Não permite saída maior que o saldo nem quantidade ≤ 0 | `Product.removeStock` / `ensureCanMoveStock` |
| Cada movimentação fica registrada com o saldo após | `StockMovement` + `StockService` |

As regras de transição de status ficam na própria entidade `Product` (camada Model), então o controller só recebe a requisição e repassa para o service.

### Perfil ADM

O projeto ainda não tem autenticação, então o perfil (**Usuário** ou **Administrador**) é escolhido na barra superior de qualquer tela e fica guardado na sessão (`SessionRole`). O botão **Descontinuar** só aparece para o ADM, e o `ProductService` também valida o perfil, então a regra vale mesmo se alguém chamar a rota direto.

## Estrutura

```
aula-6/
├── diagramas/
│   ├── sequencia-entrada-estoque.mermaid   # sequência da entrada
│   ├── sequencia-saida-estoque.mermaid     # sequência da saída
│   ├── atividades-saida-estoque.mermaid    # atividades da saída (bloqueia descontinuado e sem estoque)
│   └── estados-produto.mermaid             # estados do produto
└── mvc/
    └── src/main/java/br/com/mvc/
        ├── controller/  ProductController, StockController, RoleController, RoleModelAdvice, SessionRole, ErrorController
        └── model/
            ├── entity/      Product, StockMovement
            ├── enums/       ProductStatus, MovementType, Role
            ├── exception/   BusinessException
            ├── repository/  ProductRepository, StockMovementRepository
            └── service/     ProductService, StockService
```

## Como rodar

```bash
cd aula-6/mvc
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- App: http://localhost:8080
- Produtos: http://localhost:8080/products
- Estoque: http://localhost:8080/stock
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:mvc`)

Testes das regras: `./mvnw test` (`ProductTest` e `ProductServiceTest`).

### Roteiro rápido para demonstrar

1. Cadastre um produto → aparece como **Indisponível**, saldo 0.
2. Em Estoque, dê **entrada** de 10 → fica **Disponível**.
3. Dê **saída** de 10 → volta para **Indisponível**.
4. Tente uma saída com saldo 0 → erro de estoque insuficiente.
5. Troque o perfil para **Administrador** e descontinue o produto.
6. Tente movimentar o produto descontinuado → erro.

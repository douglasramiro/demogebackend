# OrderHub

Serviço backend de gerenciamento de pedidos com **CRUD em memória** e documentação interativa **Swagger / OpenAPI 3.0**, desenvolvido de acordo com as diretrizes corporativas de **Padrões de Arquitetura de Backend**.

---

## 1. Stack Tecnológica e Padrões Aplicados

- **Java 21 LTS**:
  - **Java Records** para imutabilidade de Entidades de Domínio (`Order`, `OrderItem`), Commands de Aplicação (`CreateOrderCommand`, `UpdateOrderCommand`) e DTOs REST (`CreateOrderRequest`, `OrderResponse`, `ErrorResponse`).
  - **Sealed Classes** para hierarquia fechada de exceções de negócio (`DomainException` selando `OrderNotFoundException`, `OrderValidationException` e `InvalidOrderStateException`).
  - **Pattern Matching para `switch`** no controle de transições de estado (`OrderStatus`) e no mapeamento exaustivo de exceções de domínio para códigos HTTP (`GlobalExceptionHandler`).
  - **Virtual Threads (Project Loom)** para processamento não-bloqueante nos endpoints REST (`@ExecuteOn(TaskExecutors.BLOCKING)`).
- **Micronaut Framework 4.x (AOT)**:
  - Injeção de dependências (IoC), serialização JSON (`micronaut-serde-jackson`) e geração de especificação OpenAPI 3.0 (`micronaut-openapi`) realizadas em tempo de compilação (**Ahead-Of-Time**), sem uso de reflexão em runtime.
- **Clean Architecture & Inversão de Dependência (DIP)**:
  - As camadas `domain` e `application` são **100% Java puro** (zero anotações ou imports de Micronaut, Jackson ou Swagger).
  - O wiring de injeção de dependência dos casos de uso é realizado exclusivamente na camada `infrastructure/config` via `@Factory` (`UseCaseFactory`).

---

## 2. Estrutura de Pacotes

```text
com.empresa.orderhub/
├── domain/                          # Regras de Negócio Puras (Java 21 puro)
│   ├── model/                       # Entidades e Value Objects (Order, OrderItem, OrderStatus)
│   └── exception/                   # Exceções Seladas de Domínio (DomainException e subtipos)
├── application/                     # Casos de Uso e Contratos (Java 21 puro)
│   ├── port/
│   │   ├── in/                      # Input Ports (CreateOrderUseCase, GetOrderUseCase, etc.)
│   │   └── out/                     # Output Ports (OrderRepositoryPort)
│   └── usecase/                     # Implementações dos Casos de Uso (Interactors)
├── adapter/                         # Adaptadores de Entrada e Saída
│   ├── in/
│   │   └── rest/                    # Micronaut Controller, DTOs, Mapper e GlobalExceptionHandler
│   └── out/
│       └── persistence/
│           └── memory/              # Repositório CRUD em memória (InMemoryOrderRepository)
└── infrastructure/                  # Configurações de Framework e Fábricas
    └── config/                      # UseCaseFactory (@Factory) e OpenApiConfig
```

---

## 3. Como Executar e Testar

### Executar os testes (Unitários Puros + Integração HTTP/Swagger)
```bash
mvn test
```

### Iniciar a aplicação
```bash
mvn exec:exec
# ou gerando o fat-jar executável:
mvn package -DskipTests && java -jar target/orderhub-1.0.0.jar
```

A aplicação será iniciada na porta `8080`:
- **Swagger UI Interativo**: `http://localhost:8080/swagger-ui/index.html`
- **Especificação OpenAPI 3.0 (YAML)**: `http://localhost:8080/swagger/orderhub-api-1.0.0.yml`

---

## 4. Endpoints do CRUD (`/api/v1/orders`)

| Método | Rota | Descrição | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Cria um novo pedido em memória | `201 Created`, `400 Bad Request` |
| `GET` | `/api/v1/orders` | Lista todos os pedidos (filtro opcional `?status=PENDING`) | `200 OK` |
| `GET` | `/api/v1/orders/{id}` | Busca um pedido pelo UUID | `200 OK`, `404 Not Found` |
| `PUT` | `/api/v1/orders/{id}` | Atualiza dados e itens de um pedido existente | `200 OK`, `400`, `404`, `422` |
| `PATCH` | `/api/v1/orders/{id}/status` | Altera o status do pedido respeitando máquina de estados | `200 OK`, `400`, `404`, `422` |
| `DELETE` | `/api/v1/orders/{id}` | Remove um pedido do repositório em memória | `204 No Content`, `404 Not Found` |

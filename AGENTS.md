# AGENTS.md — Diretrizes Arquiteturais para Agentes de IA (Antigravity)

Este arquivo define as regras arquiteturais, padrões de código e fluxo de implementação **obrigatórios** para qualquer nova funcionalidade, correção ou evolução no projeto **OrderHub** (`com.empresa.orderhub`).

Todo agente de IA (como o **Antigravity**) deve seguir rigorosamente estas diretrizes antes de criar ou modificar qualquer arquivo no repositório.

---

## 1. Stack Tecnológica Mandatória

1. **Linguagem e Runtime**: **Java 21 LTS**.
   - **Java Records**: Uso obrigatório para modelar Entidades de Domínio imutáveis, Value Objects, Commands de Casos de Uso e DTOs REST (`Request`/`Response`).
   - **Sealed Classes / Interfaces**: Uso obrigatório para hierarquias fechadas de domínio, especialmente exceções de negócio (`DomainException`) e máquinas de estado.
   - **Pattern Matching para `switch`**: Uso obrigatório para transições de estado, regras de negócio condicionais e mapeamento exaustivo de exceções no `GlobalExceptionHandler` (sem cláusulas `default` desnecessárias quando a hierarquia é `sealed`).
   - **Virtual Threads (Project Loom)**: Todo controller ou adaptador de I/O bloqueante deve executar sobre Virtual Threads via `@ExecuteOn(TaskExecutors.BLOCKING)`.
2. **Framework de Backend**: **Micronaut Framework 4.x (AOT)**.
   - Injeção de Dependências (IoC), AOP, validação e serialização JSON (`micronaut-serde-jackson` com `@Serdeable`) são processadas em tempo de compilação (**Ahead-Of-Time**), sem reflexão em tempo de execução.
3. **Documentação de API**: **OpenAPI 3.0 / Swagger (`micronaut-openapi`)**.
   - Todo novo endpoint ou DTO REST deve possuir anotações completas do Swagger (`@Operation`, `@ApiResponses`, `@Parameter`, `@Schema`, `@Tag`) para geração automática do arquivo YAML e do Swagger UI em `/swagger-ui/index.html`.
4. **Camada de Persistência**:
   - **Em Memória (prototipação/validação)**: Implementações thread-safe com `ConcurrentHashMap` em `adapter/out/persistence/memory/`.
   - **Relacional (SQL)**: **PostgreSQL** via Micronaut Data JDBC ou R2DBC em `adapter/out/persistence/postgres/` (para dados estruturados, integridade referencial e transações ACID).
   - **NoSQL (Coluna Larga)**: **Google Cloud Bigtable** em `adapter/out/persistence/bigtable/` (para telemetria, séries temporais, logs de auditoria em alta escala e latência sub-milissegundo).

---

## 2. Clean Architecture e Regra de Dependência (Inviolável)

A direção das dependências é estritamente **de fora para dentro**:
$$\text{Infrastructure / Adapters} \longrightarrow \text{Application (Use Cases \& Ports)} \longrightarrow \text{Domain (Entities \& Business Rules)}$$

### Regras Inegociáveis de Isolamento
1. **Domínio 100% Puro (`domain/`)**:
   - **PROIBIDO** importar ou utilizar anotações do Micronaut (`@Singleton`, `@Inject`, `@Serdeable`, `@Introspected`), Jackson, JPA/JDBC ou Swagger (`@Schema`) dentro de `com.empresa.orderhub.domain`.
   - Entidades e Value Objects devem validar suas próprias invariantes no construtor compacto do `record` e lançar subclasses de `DomainException`.
2. **Aplicação 100% Pura (`application/`)**:
   - **PROIBIDO** usar `@Singleton` ou `@Inject` nas classes de implementação de casos de uso (`*Interactor` em `application/usecase/`).
   - Os casos de uso dependem exclusivamente de interfaces de portas (`application/port/in/` e `application/port/out/`). Nunca injete drivers SQL, clientes Bigtable ou classes HTTP diretamente no caso de uso.
   - As camadas `domain` e `application` devem sempre compilar com `javac --release 21` puro, sem nenhuma biblioteca externa no classpath.
3. **Injeção de Dependência via `@Factory` (`infrastructure/config/`)**:
   - Todo novo caso de uso (`*Interactor`) deve ser registrado como bean `@Singleton` dentro de `UseCaseFactory.java` (ou nova classe `@Factory` em `infrastructure/config/`), recebendo suas portas de saída (`*Port`) por parâmetro.
4. **Tratamento Global de Exceções (`adapter/in/rest/exception/`)**:
   - O domínio nunca retorna códigos HTTP. Ele dispara exceções de negócio que estendem a classe selada `DomainException`.
   - O `GlobalExceptionHandler` intercepta `DomainException` e utiliza Pattern Matching (`switch`) para converter em respostas HTTP padronizadas (`ErrorResponse`):
     - `OrderValidationException` (ou erros de validação de entrada/domínio) $\rightarrow$ **HTTP 400 Bad Request**
     - `OrderNotFoundException` (ou recurso não encontrado) $\rightarrow$ **HTTP 404 Not Found**
     - `InvalidOrderStateException` (ou violação de regra/estado de negócio) $\rightarrow$ **HTTP 422 Unprocessable Entity**

---

## 3. Estrutura Padrão de Pacotes

Toda nova funcionalidade deve respeitar exatamente esta árvore de pacotes:

```text
com.empresa.orderhub/
├── domain/                          # Regras de Negócio Puras (ZERO dependências externas)
│   ├── model/                       # Entidades e Value Objects imutáveis (Java Records, Enums)
│   └── exception/                   # Hierarquia selada (sealed) DomainException
├── application/                     # Orquestração de Casos de Uso (ZERO dependências externas)
│   ├── port/
│   │   ├── in/                      # Input Ports (Interfaces de Casos de Uso + Command Records)
│   │   └── out/                     # Output Ports (Interfaces de Repositórios e Gateways)
│   └── usecase/                     # Implementações dos Casos de Uso (*Interactor em Java puro)
├── adapter/                         # Interface Adapters (Conversão mundo externo <-> aplicação)
│   ├── in/
│   │   └── rest/                    # Micronaut Controllers (@ExecuteOn), DTOs (@Serdeable), Mappers e ExceptionHandler
│   │       ├── dto/
│   │       ├── mapper/
│   │       └── exception/
│   └── out/
│       ├── persistence/
│       │   ├── memory/              # Repositórios em memória (ConcurrentHashMap)
│       │   ├── postgres/            # Repositórios PostgreSQL (Micronaut Data JDBC/R2DBC)
│       │   └── bigtable/            # Adaptadores Google Cloud Bigtable
│       └── messaging/               # Produtores/Consumidores de Eventos (Kafka / Pub/Sub)
└── infrastructure/                  # Configurações de Framework e Fábricas IoC
    └── config/                      # Classes @Factory (UseCaseFactory) e OpenApiConfig
```

---

## 4. Passo a Passo para Implementar uma Nova Feature (Checklist para o Agente)

Sempre que receber uma User Story ou solicitação de feature (ex.: *Cálculo de Frete e Cupom de Desconto*, *Cancelamento com Estorno*, *Novo Recurso REST*), siga esta ordem:

### Passo 1: Modelagem no Domínio (`domain/`)
1. Crie ou atualize os `record`s e `enum`s em `domain/model/` com validações intrínsecas no construtor compacto e métodos imutáveis (retornando uma nova instância do `record`).
2. Se houver um novo tipo de erro de negócio, crie a exceção `final` em `domain/exception/` estendendo `DomainException` e adicione-a à cláusula `permits` de `DomainException`.

### Passo 2: Contratos e Caso de Uso (`application/`)
1. Defina ou atualize o **Input Port** em `application/port/in/` (interface + `record` de Command, se aplicável).
2. Se precisar de novas operações de persistência ou integração externa, adicione o método no **Output Port** em `application/port/out/`.
3. Implemente a lógica de orquestração no `*Interactor` dentro de `application/usecase/` **sem nenhuma anotação do Micronaut**.

### Passo 3: Wiring na Infraestrutura (`infrastructure/config/`)
1. Caso tenha criado um novo Caso de Uso (`*UseCase` / `*Interactor`), declare o método `@Singleton` correspondente em `UseCaseFactory.java`.

### Passo 4: Adaptadores de Saída e Entrada (`adapter/`)
1. Implemente os novos métodos do Output Port no adaptador de persistência (ex.: `InMemoryOrderRepository` em `adapter/out/persistence/memory/`).
2. Crie ou atualize os DTOs REST (`record`s com `@Serdeable` e `@Schema`) em `adapter/in/rest/dto/`.
3. Atualize o mapper (`OrderRestMapper`) em `adapter/in/rest/mapper/` para converter `Request DTO -> Command` e `Domain Entity -> Response DTO`.
4. Adicione ou atualize o endpoint no Controller REST (`adapter/in/rest/`) com `@ExecuteOn(TaskExecutors.BLOCKING)` e anotações completas do OpenAPI/Swagger (`@Operation`, `@ApiResponses`).
5. Se criou uma nova subclasse de `DomainException`, adicione o `case` correspondente no `switch` exaustivo de `GlobalExceptionHandler.java`.

### Passo 5: Testes Obrigatórios (`src/test/java/`)
Toda nova feature **deve** incluir dois níveis de testes antes de ser considerada pronta:
1. **Teste Unitário Puro (`application/usecase/*UnitTest.java`)**:
   - Testa as regras do domínio e o `*Interactor` diretamente em memória, **sem `@MicronautTest`** (execução instantânea e desacoplada).
2. **Teste de Integração REST (`adapter/in/rest/*ControllerTest.java`)**:
   - Com `@MicronautTest` e `HttpClient`, validando o contrato JSON, códigos de status HTTP de sucesso (`200`, `201`, `204`) e cenários de erro (`400`, `404`, `422`).

---

## 5. Comandos de Validação e Build

Antes de finalizar qualquer tarefa ou realizar commit, execute:

```bash
# 1. Executar todos os testes unitários e de integração + geração AOT do OpenAPI
mvn test

# 2. Verificar que domain e application continuam 100% livres de dependências de framework
mkdir -p /tmp/pure-javac-check && \
  javac --release 21 -d /tmp/pure-javac-check $(find src/main/java/com/empresa/orderhub/domain src/main/java/com/empresa/orderhub/application -name "*.java") && \
  rm -rf /tmp/pure-javac-check

# 3. Gerar o fat-jar executável para container / Cloud Run
mvn package -DskipTests
```

# produto-crud-web — TP2

Sistema CRUD de gerenciamento de produtos com interface web e API REST, desenvolvido com Java 21 / Spring Boot 3.2.3.

**Disciplina:** Engenharia Disciplinada de Software — INFNET

---

## Sumário

1. [Pré-requisitos](#pré-requisitos)
2. [Iniciar o sistema](#iniciar-o-sistema)
3. [Acessar a aplicação](#acessar-a-aplicação)
4. [Executar os testes](#executar-os-testes)
5. [Cobertura de código](#cobertura-de-código)
6. [Estrutura do projeto](#estrutura-do-projeto)
7. [Entidade `Produto`](#entidade-produto)
8. [Categorias de produto](#categorias-de-produto)
9. [Promoções](#promoções)
10. [API REST](#api-rest)
11. [Regras de negócio](#regras-de-negócio)
12. [Decisões de design](#decisões-de-design)

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java (JDK) | 21+ |
| Maven | 3.8+ |
| Docker Desktop | 24+ |

---

## Iniciar o sistema

### Opção 1 — Tudo em Docker (banco + aplicação)

```bash
docker compose up --build
```

O `docker-compose.yml` sobe três containers:

| Serviço | Imagem | Porta |
|---------|--------|-------|
| `db` | postgres:16-alpine | `5434` |
| `app` | build local | `8080` |
| `pgadmin` | dpage/pgadmin4 | `5050` |

### Opção 2 — Desenvolvimento local (banco Docker + app local)

```bash
# 1. Subir apenas o banco
docker compose up db -d
docker compose ps    # aguardar healthy

# 2. Iniciar a aplicação (perfil dev — DataLoader popula produtos de exemplo)
mvn spring-boot:run
```

### Parar tudo

```bash
docker compose stop

# Parar e apagar volumes (remove dados)
docker compose down -v
```

---

## Acessar a aplicação

| Serviço | URL |
|---------|-----|
| **Interface web** | http://localhost:8080/produtos |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **API REST** | http://localhost:8080/api/v1/produtos |
| **Health check** | http://localhost:8080/actuator/health |
| **pgAdmin** | http://localhost:5050 |

**Credenciais pgAdmin:** `admin@admin.com` / `admin`
**Banco:** host `db`, porta `5432`, usuário `postgres`, senha `postgres`, db `produto_db`

---

## Executar os testes

Os testes usam H2 in-memory e não precisam do banco rodando.

```bash
mvn test
```

Saída esperada:

```
Tests run: 30  — ProdutoServiceTest       (Mockito)
Tests run: 16  — ProdutoControllerTest    (MockMvc — interface HTML)
Tests run: 15  — ProdutoRestControllerTest (MockMvc — API REST)
Tests run: 20  — ProdutoTest             (domínio — entidade)
Tests run: 11  — SkuGeneratorTest        (geração de SKU)
Tests run:  3  — CategoriaProdutoTest    (enum de categoria)
───────────────────────────────────────────
BUILD SUCCESS  (95 testes no total, excluindo Selenium)
```

### Testes Selenium E2E (requer aplicação rodando)

```bash
# Em um terminal: iniciar a aplicação
mvn spring-boot:run

# Em outro terminal: executar Selenium
mvn test -Dtest=ProdutoSeleniumTest
```

> O Chrome deve estar instalado. O WebDriverManager baixa o ChromeDriver automaticamente.

### Rodar uma classe específica

```bash
mvn test -Dtest=ProdutoServiceTest
mvn test -Dtest=ProdutoRestControllerTest
mvn test -Dtest=ProdutoTest
```

---

## Cobertura de código

Após `mvn test`, o relatório JaCoCo é gerado em:

```
target/site/jacoco/index.html
```

```bash
# Abrir no Windows
start target\site\jacoco\index.html
```

### Resultado obtido

| Métrica | Mínimo exigido | Resultado |
|---------|----------------|-----------|
| **INSTRUCTION coverage** | 80% | **≥ 80%** ✅ |

### O que está excluído da cobertura

| Excluído | Motivo |
|----------|--------|
| `ProdutoCrudApplication.class` | Bootstrap Spring Boot |
| `br/com/infnet/dto/*.class` | Apenas campos Lombok |
| `br/com/infnet/config/*.class` | Configuração trivial |
| `br/com/infnet/mapper/*.class` | Código gerado pelo MapStruct |
| Código Lombok (`@Generated`) | Getters/setters/construtores |

---

## Estrutura do projeto

```
produto-crud-web/
├── src/main/java/br/com/infnet/
│   ├── ProdutoCrudApplication.java
│   ├── config/
│   │   ├── DataLoader.java              Seed de produtos de exemplo (perfil dev)
│   │   └── JpaAuditingConfig.java       @EnableJpaAuditing
│   ├── domain/
│   │   ├── Produto.java                 Entidade rica com comandos de domínio
│   │   ├── Promocao.java                Value object @Embedded
│   │   ├── CategoriaProduto.java        Enum: 6 categorias com label
│   │   ├── TipoOperacaoEstoque.java     Enum: ENTRADA / SAIDA
│   │   ├── SkuGenerator.java            Geração de SKU a partir do nome
│   │   └── exception/
│   │       ├── DomainException.java
│   │       └── ProdutoNaoEncontradoException.java
│   ├── dto/
│   │   ├── ProdutoRequest.java          Entrada: criar/atualizar (Bean Validation)
│   │   ├── ProdutoResponse.java         Saída: inclui precoPromocional e nomeCategoria
│   │   ├── AjusteEstoqueRequest.java    Entrada: ajuste de estoque
│   │   └── PromocaoRequest.java         Entrada: ativar promoção
│   ├── factory/
│   │   └── ProdutoFactory.java          Construção e atualização de Produto
│   ├── mapper/
│   │   └── ProdutoMapper.java           Interface MapStruct (Produto ↔ DTOs)
│   ├── repository/
│   │   └── ProdutoRepository.java       Spring Data JPA + queries customizadas
│   ├── service/
│   │   └── ProdutoService.java          Regras de negócio, estoque e promoções
│   └── controller/
│       ├── ProdutoController.java       Interface web Thymeleaf (/produtos)
│       ├── ProdutoRestController.java   API REST (/api/v1/produtos)
│       ├── GlobalExceptionHandler.java  Erros MVC → redirect + flash message
│       └── RestExceptionHandler.java    Erros REST → RFC 7807 ProblemDetail
│
├── src/main/resources/
│   ├── application.properties           Perfil padrão (dev)
│   ├── application-dev.properties       PostgreSQL localhost:5434
│   ├── application-docker.properties    PostgreSQL db:5432
│   ├── application-prod.properties      Variáveis de ambiente
│   └── templates/produtos/
│       ├── lista.html                   Listagem com filtros e paginação
│       ├── form.html                    Formulário criar/editar
│       └── detalhe.html                 Detalhe com gestão de promoção
│
└── src/test/java/br/com/infnet/
    ├── service/ProdutoServiceTest.java         30 testes Mockito
    ├── controller/
    │   ├── ProdutoControllerTest.java          16 testes MockMvc (MVC)
    │   └── ProdutoRestControllerTest.java      15 testes MockMvc (REST)
    ├── domain/
    │   ├── ProdutoTest.java                    20 testes de domínio
    │   ├── SkuGeneratorTest.java               11 testes do gerador de SKU
    │   └── CategoriaProdutoTest.java            3 testes do enum
    └── selenium/ProdutoSeleniumTest.java       Testes E2E (requer Chrome)
```

---

## Entidade `Produto`

| Campo | Tipo | Restrições |
|-------|------|-----------|
| `id` | UUID | PK, imutável, gerado automaticamente |
| `nome` | String (255) | obrigatório, único (case-insensitive) |
| `descricao` | String (1000) | opcional |
| `sku` | String (50) | obrigatório, único, **gerado automaticamente** via `SkuGenerator` |
| `preco` | BigDecimal (10,2) | obrigatório, > 0 |
| `imagemUrl` | String (500) | opcional |
| `estoque` | Integer | obrigatório, ≥ 0, default 0 |
| `estoqueMinimo` | Integer | default 0; abaixo desse valor o produto é desativado |
| `ativo` | Boolean | default `true` |
| `categoria` | CategoriaProduto | opcional (enum) |
| `promocao` | Promocao (embedded) | opcional, gerenciado via comandos |
| `dataCriacao` | LocalDateTime | preenchido pelo JPA Auditing |
| `dataAtualizacao` | LocalDateTime | atualizado pelo JPA Auditing |

### Geração de SKU

O SKU é gerado automaticamente a partir do nome do produto pela classe `SkuGenerator`:

```
nome: "Monitor LG 27 Polegadas"  →  SKU: "MON-LG-27-a1b2"
nome: "Teclado Mecânico RGB"      →  SKU: "TEC-MEC-RGB-c3d4"
```

O sufixo de 4 caracteres garante unicidade mesmo para produtos com nomes similares. O SKU não é exposto no formulário de criação — é sempre derivado do nome.

---

## Categorias de produto

O enum `CategoriaProduto` classifica os produtos por tipo:

| Valor | Label exibido |
|-------|---------------|
| `MONITORES` | Monitores |
| `PERIFERICOS` | Periféricos |
| `ARMAZENAMENTO` | Armazenamento |
| `COMPONENTES` | Componentes |
| `AUDIO_VIDEO` | Áudio & Vídeo |
| `GERAL` | Geral |

A categoria é exibida como badge na listagem e permite filtro na interface web e na API REST.

---

## Promoções

Um produto pode ter exatamente uma promoção ativa por vez, representada pelo value object `Promocao` (`@Embedded`).

### Criar promoção (REST)

```http
PATCH /api/v1/produtos/{id}/promocao
Content-Type: application/json

{
  "percentualDesconto": 15.0,
  "dataInicio": "2026-03-01T00:00:00",
  "dataFim":    "2026-03-31T23:59:59"
}
```

### Encerrar promoção (REST)

```http
DELETE /api/v1/produtos/{id}/promocao
```

### Campos da promoção

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `percentualDesconto` | BigDecimal | entre 0.01 e 99.99 |
| `dataInicio` | LocalDateTime | início da vigência |
| `dataFim` | LocalDateTime | fim da vigência (deve ser após início) |
| `precoComDesconto` | BigDecimal | calculado automaticamente |

O `precoComDesconto` é calculado em `Promocao.criar()` e armazenado diretamente na tabela. A resposta da API inclui o `precoPromocional` quando há promoção ativa.

### Regras de promoção

- Produto **inativo** não pode ter promoção ativada
- `dataFim` deve ser posterior a `dataInicio`
- `percentualDesconto` deve estar entre 0,01% e 99,99%

---

## API REST

Base path: `/api/v1/produtos`

### Catálogo

| Método | Endpoint | Descrição | Retorno |
|--------|----------|-----------|---------|
| `GET` | `/` | Listar produtos ativos (paginado) | `200` |
| `GET` | `/{id}` | Buscar por ID | `200` / `404` |
| `GET` | `/sku/{sku}` | Buscar por SKU | `200` / `422` |
| `POST` | `/` | Criar produto | `201` / `400` / `422` |
| `PUT` | `/{id}` | Atualizar produto | `200` / `404` / `422` |
| `DELETE` | `/{id}` | Remover produto | `204` / `422` |

### Estoque e promoções

| Método | Endpoint | Descrição | Retorno |
|--------|----------|-----------|---------|
| `PATCH` | `/{id}/estoque` | Ajustar estoque (ENTRADA ou SAIDA) | `200` / `422` |
| `PATCH` | `/{id}/promocao` | Ativar promoção | `200` / `422` |
| `DELETE` | `/{id}/promocao` | Encerrar promoção | `200` |

### Erros

Todos os erros retornam **RFC 7807 `ProblemDetail`**:

| Status | Causa |
|--------|-------|
| `400` | Dados inválidos (Bean Validation) |
| `404` | Produto não encontrado |
| `422` | Regra de negócio violada (`DomainException`) |

Documentação interativa: http://localhost:8080/swagger-ui.html

---

## Rotas MVC (interface web)

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/produtos` | Listagem com busca e filtro por categoria |
| `GET` | `/produtos/novo` | Formulário de criação |
| `GET` | `/produtos/{id}` | Detalhe do produto |
| `GET` | `/produtos/{id}/editar` | Formulário de edição |
| `POST` | `/produtos` | Criar produto |
| `POST` | `/produtos/{id}` | Atualizar produto |
| `POST` | `/produtos/{id}/excluir` | Remover produto |
| `POST` | `/produtos/{id}/promocao` | Ativar promoção |
| `POST` | `/produtos/{id}/promocao/encerrar` | Encerrar promoção |

---

## Regras de negócio

| # | Regra | Onde é aplicada |
|---|-------|----------------|
| 1 | Produto `ativo=true` com `estoque=0` não pode ser criado/atualizado via REST | `ProdutoService.criarDTO()` / `atualizarDTO()` |
| 2 | Ao zerar estoque via SAÍDA (≤ `estoqueMinimo`), o produto é **desativado automaticamente** | `ProdutoService.ajustarEstoque()` |
| 3 | Ao repor estoque (ENTRADA) de produto inativo, ele é **reativado automaticamente** | `ProdutoService.ajustarEstoque()` |
| 4 | Produto com estoque > 0 **não pode ser removido** | `ProdutoService.remover()` |
| 5 | Produto **inativo** não pode ter promoção ativada | `Produto.ativarPromocao()` |
| 6 | `dataFim` da promoção deve ser posterior a `dataInicio` | `Promocao.criar()` |
| 7 | Nome e SKU devem ser únicos (verificação case-insensitive) | `ProdutoService.criarDTO()` |
| 8 | SKU é sempre gerado automaticamente — não pode ser informado pelo cliente | `ProdutoFactory` |

---

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Framework | Spring Boot 3.2.3 |
| Java | 21 |
| Banco (produção) | PostgreSQL 16 (Docker) |
| Banco (testes) | H2 in-memory |
| Persistência | Spring Data JPA + Hibernate |
| Mapeamento DTO | MapStruct 1.5.5 |
| Boilerplate | Lombok 1.18 |
| Templates web | Thymeleaf + Bootstrap 5.3 |
| API REST | Spring MVC (`@RestController`) |
| Documentação API | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Observabilidade | Spring Boot Actuator |
| Testes unitários | JUnit 5 + Mockito |
| Testes de integração | MockMvc (`@WebMvcTest`) |
| Testes E2E | Selenium + ChromeDriver (headless) |
| Cobertura | JaCoCo (mínimo 80% de instruções) |

---

## Decisões de design

### Entidade rica vs. anêmica
`Produto` possui comandos de domínio (`ativar()`, `desativar()`, `ativarPromocao()`, `encerrarPromocao()`) que encapsulam invariantes. O serviço coordena o fluxo; a entidade protege seu próprio estado.

### Value object `Promocao` como `@Embedded`
`Promocao` não tem identidade própria — existe apenas no contexto de um `Produto`. O `@Embedded` mantém os dados na mesma tabela sem complexidade de join, e os comandos `ativarPromocao()`/`encerrarPromocao()` são os únicos pontos de mutação.

### SKU auto-gerado por `SkuGenerator`
O SKU é derivado deterministicamente do nome do produto com um sufixo UUID de 4 caracteres. Isso elimina a necessidade de o operador inventar um código único, reduz erros de digitação e garante que o SKU sempre reflita o nome do produto.

### `JpaAuditingConfig` em classe separada
`@EnableJpaAuditing` **não** pode ficar em `ProdutoCrudApplication` quando há testes `@WebMvcTest` — esses testes carregam apenas o contexto MVC, sem JPA, o que gera erro "JPA metamodel must not be empty". A solução é mover para uma `@Configuration` dedicada.

### Dois controllers, dois handlers
`ProdutoController` (MVC/Thymeleaf) e `ProdutoRestController` (REST/JSON) têm contratos de resposta diferentes. Cada um tem seu `@ControllerAdvice` dedicado para tratamento de erros, evitando lógica condicional baseada em `Accept` header.

### Spring Profiles

| Perfil | Banco | Uso |
|--------|-------|-----|
| `dev` | PostgreSQL localhost:5434 | Desenvolvimento local |
| `docker` | PostgreSQL db:5432 | Container |
| `prod` | Variáveis de ambiente | Produção |
| `test` | H2 in-memory | Testes automatizados |

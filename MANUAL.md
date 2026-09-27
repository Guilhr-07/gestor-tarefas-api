# Manual completo — API de Gestão de Tarefas

> Manual minucioso deste projeto. Se ainda não leu, comece pelos
> **[Fundamentos](../MANUAIS/01-FUNDAMENTOS-SPRING-BOOT.md)** — aqui não repito os
> conceitos gerais (camadas, JPA, DTO, validação, ProblemDetail).

Projeto: **API REST para gerenciar tarefas** (criar, listar, concluir, apagar). É o mais
simples dos três — o lugar certo para entender a arquitetura antes dos outros.

---

## 1. Como rodar (passo a passo)

Pré-requisito: **JDK 21** (`java -version` deve mostrar 21). Não precisa de Maven.

```bash
cd gestor-tarefas-api
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw.cmd spring-boot:run
```

Quando aparecer `Started GestorTarefasApiApplication`, a API está em
**http://localhost:8080**. Já vem com 3 tarefas de exemplo.

```bash
curl http://localhost:8080/api/tarefas
curl -X POST http://localhost:8080/api/tarefas \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Minha primeira tarefa","prioridade":"ALTA"}'
```

Testes: `./mvnw test`. Parar: `Ctrl+C`.

---

## 2. Estrutura de pastas

```
gestor-tarefas-api/
├── pom.xml                     # dependências e build (Maven)
├── mvnw / mvnw.cmd             # wrapper que baixa o Maven sozinho
├── Dockerfile / compose.yaml   # empacotar e subir com PostgreSQL
├── api.http                    # requisições prontas (VS Code / IntelliJ)
├── src/main/resources/
│   ├── application.properties  # configuração (banco, porta…)
│   └── data.sql                # dados de exemplo (só no H2)
└── src/main/java/dev/guilherme/tarefas/
    ├── GestorTarefasApiApplication.java   # ponto de partida (main)
    ├── domain/     Tarefa, StatusTarefa, Prioridade
    ├── repository/ TarefaRepository
    ├── dto/        TarefaRequest, TarefaResponse
    ├── service/    TarefaService
    ├── web/        TarefaController, GlobalExceptionHandler
    └── exception/  RecursoNaoEncontradoException
```

Fluxo de um pedido: `web` → `service` → `repository` → banco. Volta pelo mesmo caminho.

---

## 3. Passeio pelo código, arquivo por arquivo

### 3.1. `domain/StatusTarefa.java` e `Prioridade.java` — enums

```java
public enum StatusTarefa { PENDENTE, EM_ANDAMENTO, CONCLUIDA }
public enum Prioridade   { BAIXA, MEDIA, ALTA }
```

Um **enum** é uma lista fechada de valores. A tarefa só pode ter um desses status — nada
de texto solto digitado errado.

### 3.2. `domain/Tarefa.java` — a entidade (a tabela)

```java
@Entity @Table(name = "tarefas")
public class Tarefa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String titulo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusTarefa status = StatusTarefa.PENDENTE;
    private LocalDate prazo;                         // pode ser null
    @Column(nullable = false, updatable = false) private Instant criadaEm;
}
```

- `@Entity`+`@Table` → tabela `tarefas`. `@Id`+`@GeneratedValue(IDENTITY)` → o banco gera o número.
- `@Enumerated(EnumType.STRING)` grava o **nome** (`"PENDENTE"`), não o número da posição —
  se você reordenar o enum um dia, os dados antigos continuam certos. **Sempre use STRING.**

Datas preenchidas sozinhas por callbacks de ciclo de vida:

```java
@PrePersist void aoCriar() { criadaEm = atualizadaEm = Instant.now(); }  // antes de inserir
@PreUpdate  void aoAtualizar() { atualizadaEm = Instant.now(); }         // antes de atualizar
```

A **regra de negócio mora na entidade** ("modelo rico"), não no controller:

```java
public void concluir() { this.status = StatusTarefa.CONCLUIDA; }
```

O `protected Tarefa()` vazio existe só porque o JPA exige — você não o usa.

### 3.3. `repository/TarefaRepository.java`

```java
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    Page<Tarefa> findByStatus(StatusTarefa status, Pageable pageable);
}
```

Só uma **interface**. Estendendo `JpaRepository` você já ganha `save/findById/findAll/
deleteById/existsById`. O `findByStatus` o Spring traduz para `WHERE status = ?` pelo
**nome do método**. `Pageable` dá paginação de graça.

### 3.4. `dto/TarefaRequest.java` — entrada validada

```java
public record TarefaRequest(
    @NotBlank @Size(min = 3, max = 120) String titulo,
    @Size(max = 500) String descricao,
    StatusTarefa status, Prioridade prioridade, LocalDate prazo) { }
```

`record` = classe curta só de dados. As anotações são as **regras**: título vazio/curto →
o pedido morre com 400 antes de chegar na regra.

### 3.5. `dto/TarefaResponse.java`

Espelha a Tarefa para o cliente, com atalho de conversão `TarefaResponse.de(tarefa)`.

### 3.6. `service/TarefaService.java` — a regra

```java
@Service
public class TarefaService {
    private final TarefaRepository repository;
    public TarefaService(TarefaRepository repository) { this.repository = repository; } // injeção

    @Transactional
    public TarefaResponse criar(TarefaRequest r) {
        Tarefa t = new Tarefa(r.titulo(), r.descricao(), r.prioridade(), r.prazo());
        return TarefaResponse.de(repository.save(t));   // save devolve com id
    }

    private Tarefa buscarEntidade(Long id) {
        return repository.findById(id)  // Optional: pode não achar
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa " + id + " não encontrada"));
    }
}
```

`@Transactional` = a operação acontece por inteiro ou não acontece. O `buscarEntidade` é
reaproveitado por buscar/atualizar/concluir — **um lugar só** decide o "não encontrado".

### 3.7. `web/TarefaController.java` — as rotas

```java
@RestController @RequestMapping("/api/tarefas")
public class TarefaController {

    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest req,
                                                UriComponentsBuilder uri) {
        TarefaResponse criada = service.criar(req);
        URI location = uri.path("/api/tarefas/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);   // 201 + Location
    }

    @GetMapping   // ?status=PENDENTE&page=0&size=20&sort=prazo,asc
    public Page<TarefaResponse> listar(@RequestParam(required = false) StatusTarefa status,
                                       @PageableDefault(size = 20) Pageable pageable) {
        return service.listar(status, pageable);
    }

    @PatchMapping("/{id}/concluir")
    public TarefaResponse concluir(@PathVariable Long id) { return service.concluir(id); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)   // 204
    public void remover(@PathVariable Long id) { service.remover(id); }
}
```

- `@RequestBody` converte o JSON em objeto; `@Valid` dispara as regras.
- `ResponseEntity.created(location)` → 201 + cabeçalho `Location` apontando pra nova tarefa.
- `@PathVariable` pega o número da URL (`/api/tarefas/5` → `id = 5`).

### 3.8. `web/GlobalExceptionHandler.java` — erros centralizados

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        var p = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        p.setTitle("Recurso não encontrado");
        return p;   // vira 404 no formato ProblemDetail
    }
}
```

Exceção lançada em qualquer lugar → cai aqui → vira a resposta certa. O controller fica
limpo, sem `try/catch`.

### 3.9. `application.properties`

```properties
spring.datasource.url=${DB_URL:jdbc:h2:mem:tarefas;DB_CLOSE_DELAY=-1}
spring.jpa.hibernate.ddl-auto=update
management.endpoints.web.exposure.include=health,info
```

`${DB_URL:...}` = "use a variável `DB_URL`; se não existir, use H2". É assim que o mesmo
código roda com H2 em dev e PostgreSQL em produção — só mudando a variável de ambiente.

---

## 4. Como mexer (receitas)

**Adicionar um campo (ex.: `responsavel`):** (1) campo + getter em `Tarefa`; (2) no
`TarefaRequest`; (3) no `TarefaResponse` e no `de(...)`; (4) passar no `service.criar`.
Rode `./mvnw test`. Com `ddl-auto=update` a coluna aparece sozinha.

**Adicionar um endpoint (ex.: reabrir):** (1) `reabrir()` na `Tarefa`; (2) método no
service; (3) `@PatchMapping("/{id}/reabrir")` no controller.

**Mudar uma validação:** edite a anotação no `TarefaRequest` (ex.: `@Size(min = 5)`).

---

## 5. Os testes explicados (`TarefaApiIntegrationTest`)

`@SpringBootTest` sobe a app inteira; `@AutoConfigureMockMvc` dá um "navegador de mentira".
Cada `@Test` verifica um comportamento:

```java
mockMvc.perform(post("/api/tarefas").content("{ \"titulo\": \"ab\" }"))
       .andExpect(status().isBadRequest())            // título curto → 400
       .andExpect(jsonPath("$.erros.titulo").exists());
```

São 4 testes de fluxo + 1 de "a app sobe". `./mvnw test` deve terminar com
`Tests run: 5 … BUILD SUCCESS`.

---

## 6. Problemas comuns

| Sintoma | Causa | Solução |
|---------|-------|---------|
| `port 8080 already in use` | Porta ocupada | `--server.port=8081` ou feche o outro |
| `invalid target release: 21` | JDK < 21 | Instale o JDK 21 |
| `./mvnw: Permission denied` | wrapper sem permissão | `chmod +x mvnw` |
| Campo novo não aparece | H2 reiniciou | Reinicie a API (H2 em memória recria) |

Próximo: **[Controle Financeiro](../controle-financeiro-api/MANUAL.md)** (Mês 3).

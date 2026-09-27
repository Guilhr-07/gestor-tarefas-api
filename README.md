# Gestor de Tarefas API

API REST para criar, listar, concluir e apagar tarefas, com filtro por status e paginação. Feita em Java 21 e Spring Boot 4.1.

## Por que existe

É o primeiro dos três projetos da minha trilha de backend (Mês 2). O domínio é pequeno de propósito: uma entidade, dois enums. Assim sobra atenção para o que costuma ficar mal feito em projeto de estudo: contrato HTTP certo (201 com `Location`, 204, 404), erro em formato padrão, validação no servidor, teste de integração e schema versionado.

## Como funciona

1. O cliente cria uma tarefa com `POST /api/tarefas`. Ela nasce `PENDENTE`, e com prioridade `MEDIA` se nada for informado.
2. Lista com `GET /api/tarefas?status=PENDENTE&page=0&size=20&sort=prazo,asc`.
3. Conclui com `PATCH /api/tarefas/{id}/concluir`. Concluir de novo não muda nada.
4. Qualquer erro volta como `ProblemDetail` (RFC 9457): 400 com o mapa `erros` campo por campo, 404 com a mensagem.

## Arquitetura

```
cliente HTTP -> web (controller, handler de erro) -> service (@Transactional) -> repository (Spring Data JPA) -> H2 ou PostgreSQL
```

- `web/`: rotas e conversão de exceção em resposta. Não tem regra de negócio.
- `service/`: fronteira de transação e o "não encontrado" num lugar só (`buscarEntidade`).
- `domain/`: a entidade `Tarefa` guarda a regra (`concluir()`, `atualizar()`) e preenche `criadaEm`/`atualizadaEm` por callback JPA.
- `dto/`: records de entrada (validados) e de saída. A entidade nunca sai direto na resposta.

## Decisões técnicas

| Decisão | Por quê |
| --- | --- |
| Enum gravado como texto (`EnumType.STRING`) e com `CHECK` no banco | Reordenar o enum não corrompe dado antigo, e o banco recusa valor fora da lista |
| H2 em dev, PostgreSQL por variável de ambiente | `./mvnw spring-boot:run` funciona sem instalar nada; o mesmo jar roda no Postgres |
| Flyway só no perfil `postgres`, com `ddl-auto=validate` | Em banco de verdade o schema tem histórico; no H2 de dev o Hibernate cria as tabelas |
| `saveAndFlush` no PUT e no PATCH | O `@PreUpdate` só roda no flush; sem ele a resposta saía com o `atualizadaEm` antigo |
| `?sort=` com campo inexistente vira 400 | Antes estourava `PropertyReferenceException` e devolvia 500 |

## Rodando localmente

Pré-requisito: JDK 21 ou mais novo. Não precisa instalar Maven, o `mvnw` baixa.

```bash
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

Sobe em `http://localhost:8080` com H2 em memória e 3 tarefas de exemplo (`data.sql`).

```bash
curl -i -X POST http://localhost:8080/api/tarefas \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Estudar JPA","prioridade":"ALTA","prazo":"2026-10-10"}'
# HTTP/1.1 201, Location: http://localhost:8080/api/tarefas/4
```

Com PostgreSQL (API e banco no Docker, perfil `postgres`, schema criado pelo Flyway):

```bash
docker compose up --build
```

O arquivo [`api.http`](api.http) tem as requisições prontas para VS Code ou IntelliJ.

## Endpoints

6 rotas, todas em `TarefaController`.

| Método | Rota | Resposta |
| --- | --- | --- |
| `POST` | `/api/tarefas` | 201 + `Location`, 400 |
| `GET` | `/api/tarefas?status=&page=&size=&sort=` | 200 (página), 400 para `sort` inválido |
| `GET` | `/api/tarefas/{id}` | 200, 404 |
| `PUT` | `/api/tarefas/{id}` | 200, 400, 404 |
| `PATCH` | `/api/tarefas/{id}/concluir` | 200, 404 |
| `DELETE` | `/api/tarefas/{id}` | 204, 404 |

Além delas, `GET /actuator/health` responde com o estado do banco.

Corpo de entrada:

```json
{
  "titulo": "obrigatório, 3 a 120 caracteres",
  "descricao": "opcional, até 500",
  "status": "PENDENTE | EM_ANDAMENTO | CONCLUIDA (ignorado na criação)",
  "prioridade": "BAIXA | MEDIA | ALTA",
  "prazo": "2026-08-01"
}
```

## Testes

```bash
./mvnw verify
```

8 testes, todos de integração com `@SpringBootTest` e MockMvc sobre H2:

- `TarefaApiIntegrationTest` (6): criação com 201 e `Location`, validação 400, 404, concluir, `sort` inválido, `atualizadaEm` depois do PATCH.
- `MigracaoFlywayTest` (1): aplica as migrations num H2 em modo PostgreSQL e sobe o contexto com `ddl-auto=validate`. Se alguém criar um campo na entidade e esquecer a migration, o CI quebra aqui.
- `GestorTarefasApiApplicationTests` (1): o contexto sobe.

Não há teste unitário isolado nem medição de cobertura. O CI (`.github/workflows/ci.yml`) roda `./mvnw -B verify` em todo push e PR.

## O que ficou de fora

- Autenticação. Qualquer pessoa altera qualquer tarefa. O terceiro projeto da trilha (e-commerce) trata disso com JWT.
- `PUT` é substituição completa: campo omitido vira `null` (o `prazo`, por exemplo). Um `PATCH` parcial seria mais amigável.
- O teste de migração roda em H2 no modo PostgreSQL, não num Postgres real. Testcontainers fecharia essa diferença.
- `/actuator/health` mostra detalhes (caminho de disco, banco) para qualquer um. Em produção seria `when-authorized`.
- A resposta de lista serializa o `PageImpl` inteiro, com campos internos do Spring Data. Um DTO de página próprio deixaria o contrato estável.

## Aprendizados

- `@PreUpdate` não roda quando você chama `save()` numa entidade já gerenciada; roda no flush. A resposta do PATCH mostrava a data velha, e só um teste comparando as duas datas pegou isso.
- Com Flyway, `spring.jpa.defer-datasource-initialization=true` faz o Hibernate validar o schema antes da migration rodar ("missing table"). No perfil `postgres` ele fica `false`.

## Licença

MIT, veja [LICENSE](LICENSE).

# Gestor de Tarefas — API REST (Spring Boot)

API REST para gestão de tarefas, construída como o **projeto marco do Mês 2** da trilha
de backend (Java avançado, POO, Spring Boot, APIs REST). Foco em código limpo,
validação, tratamento de erros padronizado e testes automatizados — mentalidade de
produção, não tutorial.

![build](https://img.shields.io/badge/tests-5%20passing-brightgreen) ![java](https://img.shields.io/badge/java-21-orange) ![spring](https://img.shields.io/badge/spring--boot-4.1-green)

## Stack

- **Java 21** + **Spring Boot 4.1**
- Spring Web MVC (REST) · Spring Data JPA · Bean Validation (Jakarta)
- **H2** em memória por padrão · **PostgreSQL** por variável de ambiente
- Spring Boot Actuator (health/info)
- **JUnit 5** + MockMvc (testes de integração da API)

## Como rodar

```bash
./mvnw spring-boot:run
# a API sobe em http://localhost:8080
```

Sem instalar Maven — o wrapper (`./mvnw`) baixa tudo. Requer apenas JDK 21+.

Rodar os testes:

```bash
./mvnw test
```

## Endpoints

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| `POST` | `/api/tarefas` | Cria uma tarefa | `201 Created` + `Location` |
| `GET` | `/api/tarefas` | Lista paginada (`?status=`, `?page=`, `?size=`, `?sort=`) | `200` |
| `GET` | `/api/tarefas/{id}` | Busca por id | `200` / `404` |
| `PUT` | `/api/tarefas/{id}` | Atualiza | `200` / `404` |
| `PATCH` | `/api/tarefas/{id}/concluir` | Marca como concluída | `200` / `404` |
| `DELETE` | `/api/tarefas/{id}` | Remove | `204` / `404` |

Erros seguem o padrão **RFC 7807 (ProblemDetail)**. Validação retorna `400` com o mapa
`erros` campo → mensagem.

### Modelo

```jsonc
{
  "titulo": "string (3–120, obrigatório)",
  "descricao": "string (até 500, opcional)",
  "status": "PENDENTE | EM_ANDAMENTO | CONCLUIDA",
  "prioridade": "BAIXA | MEDIA | ALTA",
  "prazo": "2026-08-01"
}
```

## Exemplos (curl)

```bash
# criar
curl -i -X POST http://localhost:8080/api/tarefas \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Estudar JPA","prioridade":"ALTA","prazo":"2026-08-10"}'

# listar só pendentes, 5 por página
curl "http://localhost:8080/api/tarefas?status=PENDENTE&size=5"

# concluir
curl -X PATCH http://localhost:8080/api/tarefas/1/concluir

# health
curl http://localhost:8080/actuator/health
```

Veja também [`api.http`](api.http) para rodar direto no VS Code / IntelliJ.

## PostgreSQL (opcional)

O `data.sql` de exemplo roda só em banco embutido (H2); em PostgreSQL o Spring pula o
seed automaticamente (`spring.sql.init.mode=embedded` por padrão). Apontar a API para
um Postgres é só definir variáveis de ambiente:

```bash
docker compose up -d db
DB_URL=jdbc:postgresql://localhost:5432/tarefas \
DB_USER=tarefas DB_PASSWORD=tarefas \
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t gestor-tarefas-api .
docker run -p 8080:8080 gestor-tarefas-api        # roda em H2
docker compose up                                  # sobe API + PostgreSQL
```

## Estrutura

```
src/main/java/dev/guilherme/tarefas
├── domain/        # entidade Tarefa + enums (Status, Prioridade)
├── repository/    # Spring Data JPA
├── dto/           # TarefaRequest / TarefaResponse (records)
├── service/       # regras de negócio (transações)
├── web/           # controller REST + tratamento global de erros
└── exception/     # exceções de domínio
```

## Próximos passos (trilha)

- Mês 3: Docker Compose com PostgreSQL + ampliar cobertura de testes (JUnit) — já preparado.
- Mês 4: autenticação JWT, upload de arquivos, paginação avançada e documentação Swagger/OpenAPI.

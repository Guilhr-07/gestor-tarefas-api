# Gestor de Tarefas API

[![CI](https://github.com/Guilhr-07/gestor-tarefas-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Guilhr-07/gestor-tarefas-api/actions/workflows/ci.yml) | Case completo: [guilherme-portfolio.dev/projetos/gestor-tarefas-api](https://guilherme-portfolio.dev/projetos/gestor-tarefas-api)

API para criar, listar, concluir e apagar tarefas, com filtro por status e paginação.

**Stack:** Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL, Flyway, Docker, JUnit 5

## Como rodar

Pré-requisito: JDK 21 ou mais novo.

```bash
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

Sobe em `http://localhost:8080` com banco em memória e 3 tarefas de exemplo. Com PostgreSQL no Docker:

```bash
docker compose up --build
```

Requisições prontas em [`api.http`](api.http).

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

8 testes de integração, rodando no CI a cada push.

## Mais detalhes

Como funciona por dentro, decisões técnicas, limites conhecidos e aprendizados: [MANUAL.md](MANUAL.md).

## Licença

MIT, veja [LICENSE](LICENSE).

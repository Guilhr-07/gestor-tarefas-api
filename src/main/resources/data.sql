-- Seed inicial para o demo. Roda após o Hibernate criar as tabelas
-- (spring.jpa.defer-datasource-initialization=true).
INSERT INTO tarefas (titulo, descricao, status, prioridade, prazo, criada_em, atualizada_em) VALUES
  ('Estudar Spring Boot', 'Concluir a trilha de backend do mês 2', 'EM_ANDAMENTO', 'ALTA', DATEADD('DAY', 7, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Configurar CI no GitHub', 'Workflow de build e testes automatizados', 'PENDENTE', 'MEDIA', DATEADD('DAY', 3, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Publicar portfólio', 'Deploy do portfólio na Vercel', 'CONCLUIDA', 'ALTA', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

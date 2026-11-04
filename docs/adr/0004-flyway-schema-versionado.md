# 0004. Schema versionado com Flyway

**Status:** Aceita

## Contexto
O banco PostgreSQL é gerenciado no Supabase, e o schema precisa ser reproduzível em qualquer ambiente.

## Decisão
Toda alteração de schema é uma migration SQL versionada do Flyway, aplicada automaticamente na subida da aplicação. Migration aplicada em ambiente compartilhado nunca é editada; uma correção vira uma nova migration.

## Alternativas consideradas
- **`ddl-auto` do Hibernate:** gera schema implícito, sem histórico e arriscado em produção.
- **Scripts manuais:** alterações sem rastreio.

## Consequências
- Positivas: histórico auditável, ambientes idênticos, revisão de schema no pull request.
- Negativas: exige disciplina para nunca alterar o banco fora das migrations.

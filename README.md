# Desafio Conta Corrente

API REST para gerenciamento de contas e movimentações financeiras.

## Stack

- Java 21
- Spring Boot
- Gradle Wrapper
- PostgreSQL
- JPA/Hibernate
- Flyway

## Módulos

```text
conta-corrente-domain/  # entidades, repositórios, serviços, DTOs e regras de negócio
conta-corrente-api/     # Spring Boot, controllers, requests, responses, mappers e exceções HTTP
```

O módulo `conta-corrente-api` depende de `conta-corrente-domain`; a dependência inversa não é permitida.

## Executar localmente

1. Inicie o PostgreSQL:

   ```bash
   docker compose up -d
   ```

2. Execute a aplicação:

   ```bash
   ./gradlew :conta-corrente-api:bootRun
   ```

3. Execute os testes:

   ```bash
   ./gradlew test
   ```

As configurações locais podem ser sobrescritas por `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`. Não versione segredos.

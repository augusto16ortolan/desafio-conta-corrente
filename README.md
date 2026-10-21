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

## Regras de negócio já definidas

- Um CPF/CNPJ normalizado possui apenas uma conta.
- O saldo inicial é zero; saque e transferência nunca podem gerar saldo negativo.
- Valores monetários usam `BigDecimal` e duas casas decimais.
- Depósitos, saques e transferências serão idempotentes por `Idempotency-Key`.
- O número da conta possui 10 algarismos e um dígito verificador calculado pelo módulo 11.

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

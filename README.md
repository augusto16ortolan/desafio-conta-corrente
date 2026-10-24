# Desafio Conta Corrente

API REST para gerenciamento de contas e movimentações financeiras.

## Stack

- Java 21
- Spring Boot
- Gradle
- PostgreSQL
- JPA/Hibernate
- Flyway

## Módulos

```text
conta-corrente-domain/  # entidades, repositórios, serviços, DTOs e regras de negócio
conta-corrente-api/     # Spring Boot, controllers, requests, responses, mappers e exceções HTTP
```

## Decisões de arquitetura

A aplicação foi separada em dois módulos para manter as responsabilidades bem definidas. O módulo `conta-corrente-domain` concentra as entidades, regras de negócio, serviços, repositórios e migrations. Já o módulo `conta-corrente-api` fica responsável pela camada HTTP, incluindo controllers, requests, responses, mappers e tratamento de erros.

Essa separação evita que regras de negócio dependam da API e facilita a evolução da aplicação, caso seja necessário expor os mesmos casos de uso por outro meio no futuro.

O PostgreSQL foi utilizado pela consistência transacional e pelos mecanismos de concorrência necessários nas movimentações. As operações de saque e transferência usam transações, idempotência e bloqueio pessimista das contas envolvidas para evitar saldo incorreto ou lançamentos duplicados em acessos simultâneos.

O Flyway mantém a evolução do banco versionada junto ao código. Para consulta de extratos, foram criados índices voltados às buscas mais frequentes por conta e data.

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

   Ao final da execução, o JaCoCo gera relatórios HTML e XML para cada módulo. Os relatórios HTML ficam em `conta-corrente-domain/build/reports/jacoco/test/html` e `conta-corrente-api/build/reports/jacoco/test/html`.

   Para gerar apenas os relatórios de cobertura, execute:

   ```bash
   ./gradlew jacocoReport
   ```

As configurações locais podem ser sobrescritas por `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`.

## Rotas da API

Todos os exemplos consideram a aplicação disponível em `http://localhost:8080`.

### Abrir uma conta

```http
POST /api/accounts
Content-Type: application/json

{
  "identityNumber": "529.982.247-25"
}
```

Retorna `201 Created` com o identificador da conta, número, dígito e saldo inicial.

### Depositar e sacar

As movimentações exigem o cabeçalho `Idempotency-Key`.

```http
POST /api/accounts/{accountId}/deposits
Idempotency-Key: deposit-20261024-001
Content-Type: application/json

{
  "amount": 100.00
}
```

```http
POST /api/accounts/{accountId}/withdrawals
Idempotency-Key: withdrawal-20261024-001
Content-Type: application/json

{
  "amount": 25.50
}
```

Ambas as rotas retornam `201 Created` com o lançamento e o saldo após a operação.

### Transferir entre contas

```http
POST /api/accounts/{accountId}/transfers
Idempotency-Key: transfer-20261024-001
Content-Type: application/json

{
  "destinationAccountId": "00000000-0000-0000-0000-000000000000",
  "amount": 50.00
}
```

O retorno `201 Created` contém os lançamentos de débito e crédito, ligados por `transferReference`.

### Consultar o extrato

```http
GET /api/accounts/{accountId}/statement?page=0&size=20&sort=occurredAt,desc
```

Também podem ser usados os filtros opcionais `startDate` e `endDate`, por exemplo:

```text
GET /api/accounts/{accountId}/statement?startDate=2026-10-01T00:00:00Z&endDate=2026-10-31T23:59:59Z
```

O tamanho da página deve estar entre 1 e 100. A resposta informa saldo atual, paginação e lançamentos encontrados.

## Idempotência e erros

Uma mesma `Idempotency-Key` pode ser repetida com a mesma operação e o mesmo valor sem criar outro lançamento. Se for reutilizada para outra operação, valor ou destino, a API retorna `409 Conflict`.

Os erros seguem este formato:

```json
{
  "timestamp": "2026-10-24T16:00:00Z",
  "status": 422,
  "error": "Unprocessable Content",
  "message": "A conta não possui saldo suficiente para esta movimentação."
}
```

| Situação | Status |
| --- | --- |
| Dados inválidos ou parâmetros de extrato inválidos | `400 Bad Request` |
| Conta não encontrada | `404 Not Found` |
| Número de identificação ou chave de idempotência em conflito | `409 Conflict` |
| Saldo insuficiente | `422 Unprocessable Content` |
| Falha temporária de persistência | `503 Service Unavailable` |

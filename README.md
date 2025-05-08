# Spring Batch Fault Tolerance & Transaction Management Study

This sample project explores three different ways to write a CSV file into a database and update related accounts using Spring Batch. The main objectives are:

* **Fault Tolerance**: Demonstrate skip, retry, and transactional rollback behavior.
* **Transaction Management**: Show how Spring Batch demarcates chunk boundaries and interacts with underlying database transactions.
* **Repository vs. JDBC**: Compare plain-JDBC writers, service-based writers, and Spring Data `RepositoryItemWriter` approaches.

---

## Prerequisites

* Java 17+
* Maven or Gradle
* PostgreSQL (or your preferred relational DB)
* Git (to clone the repo)

---

## Project Structure

* **`Reader`**: Reads `Transaction` objects from a CSV using a generic reader factory.
* **`Processor`**: Converts `Transaction` → `TransactionDTO`.
* **`Writers`**:

  1. **Method 1 (CompositeItemWriter)**: plain-JDBC batch writer for inserting `transactions_dto` + batch update for `accounts`.
  2. **Method 2 (Service-based Writer)**: an `ItemWriter<TransactionDTO>` that calls a `@Transactional` service which both saves the DTO and updates the account.
  3. **Method 3 (Repository Writers)**: two distinct steps:

     * **Step A**: uses a `RepositoryItemWriter<TransactionDTO>` to save transactions via Spring Data.
     * **Step B**: uses a `RepositoryItemWriter<Account>` to update account balances in a second step.

---

## Configuration

All batch parameters are externalized in `application.yml` under the `batch` prefix:

```yaml
batch:
  input-file: classpath:transactions.csv
  chunk-size: 10
  page-size: 10
  core-pool-size: 3
```

And which job to launch:

```yaml
app.job.name: simpleTransactionJob   # or multiThreadedJob, asyncProcessingJob, partitionedJob
```

---

## How to Run

1. Clone the repository:

   ```bash
   ```

git clone <repo-url>
cd <repo-folder>

````

2. Create the database tables:
```sql
-- transactions_dto\ nCREATE TABLE transactions_dto (
    reference       VARCHAR(50) PRIMARY KEY,
    amount          NUMERIC(18,2),
    account_number  VARCHAR(50) NOT NULL
);

-- accounts\ nCREATE TABLE accounts (
    account_number  VARCHAR(50) PRIMARY KEY,
    balance         NUMERIC(18,2) NOT NULL DEFAULT 0
);
````

3. Build & run:

   ```bash
   ```

mvn spring-boot\:run

# or

gradle bootRun

```

4. Check console logs to observe:
- Chunk boundaries
- Skip events (fault tolerance)
- JDBC vs. Repository writes
- Transaction rollbacks on errors

---

## Method Details

### 1) CompositeItemWriter (JDBC)
- **Single Step**: `simpleTransactionStep`.
- **Writers**:
- `TransactionDtoJdbcWriter` (batch insert into `transactions_dto`).
- `AccountBalanceUpdateWriter` (batch update of account balances).
- **Flow**: Composite writer invokes insert then update within the same chunk transaction.
- **Fault Tolerance**: configured to skip one `CanNotProcessItemException` per chunk.

### 2) Service-based Writer
- **Single Step**: `ServiceStep`.
- **Writer**: `TransactionAccountServiceWriter` calls a `@Transactional` service:
1. `transactionRepo.save(tx)`
2. `accountRepo.save(updatedAccount)`
- **Benefit**: clear separation of business logic in service layer.

### 3) Repository Writers (Two Steps)
- **Step A**: save all `TransactionDTO` via `RepositoryItemWriter<TransactionDTO>`.
- **Step B**: read back or map to `Account` entities and update balances via `RepositoryItemWriter<Account>`.
- **Reason**: Spring Data repositories participate directly in chunk transactions and offer declarative CRUD.

---

## Observations

1. **Chunk Transactions**: each chunk’s read–process–write cycle is wrapped in a single transaction. On failure, the entire chunk is rolled back.
2. **Fault Tolerance**: skip and retry behavior can be tuned per exception class without manual transaction handling.
3. **Performance**: Compare JDBC batch vs. repository throughput and threading strategies (single vs. multi-threaded vs. async).

---

## Next Steps

- Add multi-threaded and partitioned examples.
- Integrate metrics with Prometheus & Grafana.
- Explore retry listeners and circuit-breaker patterns.

---


```

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



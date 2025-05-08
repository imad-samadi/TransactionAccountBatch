package com.example.TransaktionsBatch.Writer;

import com.example.TransaktionsBatch.DTO.Account;
import com.example.TransaktionsBatch.DTO.TransactionDTO;
import com.example.TransaktionsBatch.Repo.AccountRepository;
import com.example.TransaktionsBatch.Repo.TransactionDtoRepository;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// THIRD METHOD USING RepositoryItemWriter
// This configuration class defines Spring Batch writers that delegate
// persistence operations to Spring Data repositories for both
// TransactionDTO and Account entities.
@Configuration
public class RepositoryWriterConfig {

    /**
     * Writer for TransactionDTO entities.
     * Uses the TransactionDtoRepository.saveAll(...) method to
     * batch-persist a collection of TransactionDTO objects.
     */
    @Bean("transactionDtoRepositoryWriter")
    public RepositoryItemWriter<TransactionDTO> transactionDtoRepositoryWriter(
            TransactionDtoRepository repo
    ) {
        RepositoryItemWriter<TransactionDTO> writer = new RepositoryItemWriter<>();
        // Inject the Spring Data repository that manages TransactionDTO
        writer.setRepository(repo);
        // Instruct the writer to invoke the saveAll method on the repository
        writer.setMethodName("saveAll");
        return writer;
    }

    /**
     * Writer for Account entities.
     * Uses the AccountRepository.saveAll(...) method to
     * batch-persist or update Account balances.
     */
    @Bean("accountRepositoryWriter")
    public RepositoryItemWriter<Account> accountRepositoryWriter(
            AccountRepository repo
    ) {
        RepositoryItemWriter<Account> writer = new RepositoryItemWriter<>();
        // Inject the Spring Data repository that manages Account
        writer.setRepository(repo);
        // Instruct the writer to invoke the saveAll method on the repository
        writer.setMethodName("saveAll");
        return writer;
    }

    // Note: we should use 2 steps in this method

}

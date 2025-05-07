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
//THIRD METHOD USING RepositoryWriter
@Configuration
public class RepositoryWriterConfig {

    @Bean("transactionDtoRepositoryWriter")
    public RepositoryItemWriter<TransactionDTO> transactionDtoRepositoryWriter(
            TransactionDtoRepository repo
    ) {
        RepositoryItemWriter<TransactionDTO> writer = new RepositoryItemWriter<>();
        writer.setRepository(repo);
        writer.setMethodName("saveAll");
        return writer;
    }
    @Bean("accountRepositoryWriter")
    public RepositoryItemWriter<Account> accountRepositoryWriter(
            AccountRepository repo
    ) {
        RepositoryItemWriter<Account> writer = new RepositoryItemWriter<>();
        writer.setRepository(repo);
        writer.setMethodName("saveAll");
        return writer;
    }


}

package com.example.TransaktionsBatch;

import com.example.TransaktionsBatch.DTO.Transaction;
import com.example.TransaktionsBatch.DTO.TransactionDTO;
import com.example.TransaktionsBatch.Execeptions.CanNotProcessItemException;
import com.example.TransaktionsBatch.Listeners.LoggingItemWriteListener;
import com.example.TransaktionsBatch.Processor.TransactionToDtoProcessor;
import com.example.TransaktionsBatch.Repo.TransactionAccountService;
import com.example.TransaktionsBatch.Writer.AccountBalanceUpdateWriter;
import com.example.TransaktionsBatch.Writer.TransactionAccountServiceWriter;
import com.example.TransaktionsBatch.Writer.TransactionDtoJdbcWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    // DataSource providing connections to the database
    private final DataSource dataSource;
    // Repository for storing batch metadata (executions, steps)
    private final JobRepository jobRepository;
    // Transaction manager for handling chunk transactions
    private final PlatformTransactionManager transactionManager;
    // Externalized batch properties (e.g. chunk size, page size, output file path)
    private final BatchProperties batchProperties;

    private final TransactionAccountService transactionAccountService;

    @Bean("TransactionDtoJdbcWriter")
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }
    @Bean("AccountBalanceUpdateWriter")
    public ItemWriter<TransactionDTO> AccountBalanceUpdateWriter() {
        return new AccountBalanceUpdateWriter(dataSource);
    }

    @Bean("CompositeItemWriter")
    public CompositeItemWriter<TransactionDTO> compositeWriter(
            @Qualifier("TransactionDtoJdbcWriter")  ItemWriter<TransactionDTO> insertWriter,
            @Qualifier("AccountBalanceUpdateWriter") ItemWriter<TransactionDTO> updateWriter) {
        CompositeItemWriter<TransactionDTO> composite = new CompositeItemWriter<>();
        // The order matters: first insert into transactions_dto, then update accounts
        composite.setDelegates(Arrays.asList(
                insertWriter,
                updateWriter
        ));
        return composite;
    }


    @Bean("TransactionToDtoProcessor")
    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
        return new TransactionToDtoProcessor()  ;
    }

    // first method using CompositeItemWriter
    @Bean("simpleTransactionStep")
    public Step simpleTransactionStep(
            @Qualifier("FlatFileItemReader") ItemReader<Transaction> transactionReader,
            ItemProcessor<Transaction, TransactionDTO> transactionProcessor,
             CompositeItemWriter<TransactionDTO> compositeWriter
            ) {

        return new StepBuilder("simpleTransactionStep", jobRepository)
                // Configure chunk size and transaction manager
                .<Transaction, TransactionDTO>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(compositeWriter)
                .listener(loggingItemWriteListener())
                .faultTolerant()
                .skip(CanNotProcessItemException.class)
                .skipLimit(1)
                .processorNonTransactional()
                //.noRollback(CanNotProcessItemException.class)
                .build();
    }



    //                       SECOND METHOD USING A SERVICE  :


    @Bean("TransactionAccountServiceWriter")
    public TransactionAccountServiceWriter transactionAccountServiceWriter() {
        return new TransactionAccountServiceWriter(transactionAccountService);
    }
    @Bean("ServiceStep")
    public Step SecondMethodStep(
            @Qualifier("FlatFileItemReader") ItemReader<Transaction> transactionReader,
            ItemProcessor<Transaction, TransactionDTO> transactionProcessor

    ) {

        return new StepBuilder("simpleTransactionStep", jobRepository)
                // Configure chunk size and transaction manager
                .<Transaction, TransactionDTO>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(transactionAccountServiceWriter())
                .listener(loggingItemWriteListener())

                .build();
    }



    @Bean
    public Job simpleTransactionJob(

            @Qualifier("ServiceStep") Step simpleTransactionStep
            ) {

        return new JobBuilder("siSSQQZl", jobRepository)
                //.incrementer(new RunIdIncrementer())

                .flow(simpleTransactionStep)
                .end()
                .build();
    }




    @Bean
    public LoggingItemWriteListener loggingItemWriteListener() {
        return new LoggingItemWriteListener();
    }





}

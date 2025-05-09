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

/**
 * Main Batch configuration defining three implementation methods:
 * 1) CompositeItemWriter with two delegates (JDBC insert + update)
 * 2) Service-based writer (save + update in a  service)
 * 3) RepositoryItemWriter-based (in separate config but should use 2 steps)
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    // DataSource for JDBC-based steps
    private final DataSource dataSource;
    // JobRepository stores job/step metadata
    private final JobRepository jobRepository;
    // Transaction manager for chunk boundaries
    private final PlatformTransactionManager transactionManager;
    // Externalized chunk size etc.
    private final BatchProperties batchProperties;

    // Service used in the second method
    private final TransactionAccountService transactionAccountService;

    // ----------------------
    // METHOD 1: CompositeItemWriter (JDBC)
    // ----------------------

    /**
     * Writer that inserts TransactionDTO rows via plain JDBC batch.
     */
    @Bean("TransactionDtoJdbcWriter")
    public ItemWriter<TransactionDTO> transactionDtoJdbcWriter() {
        return new TransactionDtoJdbcWriter(dataSource);
    }

    /**
     * Writer that updates account balances via plain JDBC batch.
     */
    @Bean("AccountBalanceUpdateWriter")
    public ItemWriter<TransactionDTO> AccountBalanceUpdateWriter() {
        return new AccountBalanceUpdateWriter(dataSource);
    }

    /**
     * Composite writer: first performs inserts then balance updates.
     */
    @Bean("CompositeItemWriter")
    public CompositeItemWriter<TransactionDTO> compositeWriter(
            @Qualifier("TransactionDtoJdbcWriter")  ItemWriter<TransactionDTO> insertWriter,
            @Qualifier("AccountBalanceUpdateWriter") ItemWriter<TransactionDTO> updateWriter) {
        CompositeItemWriter<TransactionDTO> composite = new CompositeItemWriter<>();
        // The delegate list order defines execution sequence
        composite.setDelegates(Arrays.asList(
                insertWriter,
                updateWriter
        ));
        return composite;
    }

    // ----------------------
    // Common processor bean
    // ----------------------
    /**
     * Processor converting CSV Transaction → TransactionDTO for later writing.
     */
    @Bean("TransactionToDtoProcessor")
    public ItemProcessor<Transaction, TransactionDTO> transactionToDtoProcessor() {
        return new TransactionToDtoProcessor();
    }

    /**
     * Step using composite writer (method 1).
     * Fault tolerance enabled for specific exception.
     */
    @Bean("simpleTransactionStep")
    public Step simpleTransactionStep(
            @Qualifier("FlatFileItemReader") ItemReader<Transaction> transactionReader,
            ItemProcessor<Transaction, TransactionDTO> transactionProcessor,
            CompositeItemWriter<TransactionDTO> compositeWriter
    ) {
        return new StepBuilder("simpleTransactionStep", jobRepository)
                // chunk-oriented step configuration
                .<Transaction, TransactionDTO>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(compositeWriter)
                // listener logs each write invocation
                .listener(loggingItemWriteListener())
                // enable skipping for business exception
                .faultTolerant()
                .skip(CanNotProcessItemException.class)
                .skipLimit(1)
                .processorNonTransactional()
                .build();
    }

    // ----------------------
    // METHOD 2: Service-based writer
    // ----------------------

    /**
     * Writer that delegates each item to a @Transactional service
     * which saves the transaction and updates the account.
     */
    @Bean("TransactionAccountServiceWriter")
    public TransactionAccountServiceWriter transactionAccountServiceWriter() {
        return new TransactionAccountServiceWriter(transactionAccountService);
    }

    /**
     * Step using service-based writer (method 2).
     */
    @Bean("ServiceStep")
    public Step SecondMethodStep(
            @Qualifier("FlatFileItemReader") ItemReader<Transaction> transactionReader,
            ItemProcessor<Transaction, TransactionDTO> transactionProcessor
    ) {
        return new StepBuilder("serviceStep", jobRepository)
                .<Transaction, TransactionDTO>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(transactionAccountServiceWriter())
                .listener(loggingItemWriteListener())
                .build();
    }

    // ----------------------
    // JOB DEFINITION
    // ----------------------

    /**
     * Single-job wiring ServiceStep as the flow.
     * To switch methods, you can inject different steps here.
     */
    @Bean
    public Job simpleTransactionJob(
            @Qualifier("simpleTransactionStep") Step simpleTransactionStep
    ) {
        return new JobBuilder("simpleTransactJob", jobRepository)
                .flow(simpleTransactionStep)
                .end()
                .build();
    }

    /**
     * Listener bean used to log write events in each step.
     */
    @Bean
    public LoggingItemWriteListener loggingItemWriteListener() {
        return new LoggingItemWriteListener();
    }
}

package com.example.TransaktionsBatch.Reader;

import com.example.TransaktionsBatch.BatchProperties;
import com.example.TransaktionsBatch.DTO.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for reading CSV files into Transaction objects.
 * Defines tokenizer, field mapper, and reader beans.
 */
@Configuration
@RequiredArgsConstructor
public class ReadConfig {

    // Externalized properties (file path, etc.)
    private final BatchProperties batchProperties;

    /**
     * LineTokenizer bean: splits each CSV line on commas
     * and maps columns to field names: reference, amount, currency, accountNumber.
     */
    @Bean
    @Qualifier("transactionCsvTokenizer")
    public LineTokenizer transactionCsvTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(new String[]{
                "reference",
                "amount",
                "currency",
                "accountNumber"
        });
        return tokenizer;
    }

    /**
     * FieldSetMapper bean: converts tokenized fields into a Transaction instance.
     * Uses a custom TransactionFieldSetMapper implementation.
     */
    @Bean
    @Qualifier("transactionFieldSetMapper")
    public FieldSetMapper<Transaction> transactionFieldSetMapper() {
        return new TransactionFieldSetMapper();
    }

    /**
     * FlatFileItemReader bean: reads CSV lines, tokenizes them, and maps to Transaction.
     * Uses a generic factory for reader creation to encapsulate common setup.
     */
    @Bean
    @Qualifier("FlatFileItemReader")
    public FlatFileItemReader<Transaction> transactionCsvReader(
            @Qualifier("transactionCsvTokenizer") LineTokenizer tokenizer,
            @Qualifier("transactionFieldSetMapper") FieldSetMapper<Transaction> mapper
    ) {
        // Factory encapsulates reader configuration (resource path, strict mode, etc.)
        GenericCsvReaderFactory<Transaction> readerFactory =
                new GenericCsvReaderFactory<>(
                        "transactionCsvFileReader",           // reader name
                        batchProperties.getInputFile(),       // resource path
                        true                                   // strict: fail if missing
                );

        // Create and return a fully configured FlatFileItemReader
        return readerFactory.createReader(tokenizer, mapper);
    }
}

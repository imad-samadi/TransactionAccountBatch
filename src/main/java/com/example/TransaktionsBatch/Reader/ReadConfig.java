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

@Configuration
@RequiredArgsConstructor
public class ReadConfig {

    private final BatchProperties batchProperties;

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

    @Bean
    @Qualifier("transactionFieldSetMapper")
    public FieldSetMapper<Transaction> transactionFieldSetMapper() {
        return new TransactionFieldSetMapper(); // Instantiate your custom mapper
    }

    @Bean
    @Qualifier("FlatFileItemReader")
    public FlatFileItemReader<Transaction> transactionCsvReader(
            @Qualifier("transactionCsvTokenizer") LineTokenizer tokenizer,
            @Qualifier("transactionFieldSetMapper") FieldSetMapper<Transaction> mapper
    ) {
        GenericCsvReaderFactory<Transaction> readerFactory =
                new GenericCsvReaderFactory<>(
                        "transactionCsvFileReader",
                       batchProperties.getInputFile(),
                        true
                );

        return readerFactory.createReader(tokenizer, mapper);
    }
}

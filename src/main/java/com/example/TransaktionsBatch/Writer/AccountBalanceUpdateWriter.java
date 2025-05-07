package com.example.TransaktionsBatch.Writer;

import com.example.TransaktionsBatch.DTO.TransactionDTO;
import com.example.TransaktionsBatch.Execeptions.CanNotProcessItemException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
@RequiredArgsConstructor
public class AccountBalanceUpdateWriter implements ItemWriter<TransactionDTO> {


    private final DataSource dataSource;


    private static final String UPDATE_SQL =
            "UPDATE accounts " +
                    "SET balance = balance + :amount " +
                    "WHERE account_number = :accountNumber";

    private JdbcBatchItemWriter<TransactionDTO> delegate;

    private void initDelegate() {
        if (delegate == null) {
            delegate = new JdbcBatchItemWriterBuilder<TransactionDTO>()
                    .dataSource(dataSource)
                    .sql(UPDATE_SQL)
                    // Maps bean properties onto the named parameters
                    .beanMapped()
                    .build();
            delegate.afterPropertiesSet();
        }
    }

    @Override
    public void write(Chunk<? extends TransactionDTO> chunk) throws Exception {
        boolean containsTriggerId = chunk.getItems().stream()
                .anyMatch(item -> item.getReference().equals("REF013"));
        if (containsTriggerId) {
            throw new CanNotProcessItemException("Simulated writer error triggered by item with ID ");
        } else {

        initDelegate();
        delegate.write(chunk);}

    }
}

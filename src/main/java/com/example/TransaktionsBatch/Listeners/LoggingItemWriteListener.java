package com.example.TransaktionsBatch.Listeners;

import com.example.TransaktionsBatch.DTO.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
@Slf4j
public class LoggingItemWriteListener implements ItemWriteListener<TransactionDTO> {

    @Override
    public void beforeWrite(Chunk<? extends TransactionDTO> items) {
        log.info("About to write {} items to the database. References = {}",
                items.size(),
                items.getItems().stream()
                        .map(TransactionDTO::getReference)
                        .toList());
    }
    @Override
    public void afterWrite(Chunk<? extends TransactionDTO> items) {
        log.info("Successfully wrote {} items. References = {}",
                items.size(),
                items.getItems().stream()
                        .map(TransactionDTO::getReference)
                        .toList());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends TransactionDTO> items) {
        log.error("Error writing {} items. References = {}. Exception: {}",
                items.size(),
                items.getItems().stream()
                        .map(TransactionDTO::getReference)
                        .toList(),
                exception.getMessage(), exception);
    }
}

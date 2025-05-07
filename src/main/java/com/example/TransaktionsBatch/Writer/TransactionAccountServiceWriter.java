package com.example.TransaktionsBatch.Writer;

import com.example.TransaktionsBatch.DTO.TransactionDTO;
import com.example.TransaktionsBatch.Repo.TransactionAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class TransactionAccountServiceWriter implements ItemWriter<TransactionDTO> {

    private final TransactionAccountService service;


    @Override
    public void write(Chunk<? extends TransactionDTO> chunk) throws Exception {

        for (TransactionDTO tx : chunk.getItems()) {

            service.saveTransactionAndUpdateAccount(tx);
        }
    }
}

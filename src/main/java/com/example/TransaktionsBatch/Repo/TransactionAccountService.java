package com.example.TransaktionsBatch.Repo;

import com.example.TransaktionsBatch.DTO.Account;
import com.example.TransaktionsBatch.DTO.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionAccountService {

    private final TransactionDtoRepository transactionRepo;
    private final AccountRepository        accountRepo;

    public void saveTransactionAndUpdateAccount(TransactionDTO tx) {

        transactionRepo.save(tx);


        Account account = accountRepo.findById(tx.getAccountNumber()).orElseThrow(() -> new RuntimeException("Account not found"));



        BigDecimal newBalance = account.getBalance().add(tx.getAmount());
        account.setBalance(newBalance);


        accountRepo.save(account);
    }
}

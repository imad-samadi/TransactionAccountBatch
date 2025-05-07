package com.example.TransaktionsBatch.Repo;

import com.example.TransaktionsBatch.DTO.TransactionDTO;
import org.springframework.data.repository.CrudRepository;

public interface TransactionDtoRepository extends CrudRepository<TransactionDTO, String> {
}

package com.example.TransaktionsBatch.Repo;

import com.example.TransaktionsBatch.DTO.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, String> {
}

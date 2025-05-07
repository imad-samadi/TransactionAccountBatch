package com.example.TransaktionsBatch.DTO;

import lombok.*;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    @ToString.Include
    private String reference;
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
}

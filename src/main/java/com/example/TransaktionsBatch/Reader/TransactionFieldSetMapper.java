package com.example.TransaktionsBatch.Reader;

import com.example.TransaktionsBatch.DTO.Transaction;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class TransactionFieldSetMapper implements FieldSetMapper<Transaction> {
    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {

        if (fieldSet == null) {
            return null;
        }

        Transaction.TransactionBuilder builder = Transaction.builder();



        builder.reference(fieldSet.readString("reference"));


        builder.amount(fieldSet.readBigDecimal("amount"));

        builder.currency(fieldSet.readString("currency"));
        builder.accountNumber(fieldSet.readString("accountNumber"));



        return builder.build();
    }
    }

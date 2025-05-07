package com.example.TransaktionsBatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)


public class TransactionsBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsBatchApplication.class, args);
	}

}

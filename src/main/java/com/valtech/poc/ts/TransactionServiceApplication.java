package com.valtech.poc.ts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.cloud.openfeign.EnableFeignClients;


@OpenAPIDefinition(
    info = @Info(
        title = "Transaction Service API",
        version = "1.0",
        description = "Handles transaction initiation and completion"
    )
)
@SpringBootApplication
@EntityScan(basePackages = "com.valtech.poc.*")
@EnableFeignClients
public class TransactionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

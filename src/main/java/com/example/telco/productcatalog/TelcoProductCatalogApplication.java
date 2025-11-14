package com.example.telco.productcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.telco.productcatalog.service.ProductImportService;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class TelcoProductCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelcoProductCatalogApplication.class, args);
	}

	@Bean
	CommandLineRunner initProducts(ProductImportService importService) {
		return args -> importService.importAll();
	}

}

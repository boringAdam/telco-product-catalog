package com.example.telco.productcatalog.service;

import com.example.telco.productcatalog.domain.Product;
import com.example.telco.productcatalog.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductRepository productRepository;

    public void importAll() {
        importFromCsv("incompatible_feeds_products.csv");
        importFromJson("incompatible_feeds_products.json");
    }

    private void importFromCsv(String resourceName) {
    }

    private void importFromJson(String resourceName) {
    }

}

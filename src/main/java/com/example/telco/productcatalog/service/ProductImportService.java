package com.example.telco.productcatalog.service;

import com.example.telco.productcatalog.domain.Product;
import com.example.telco.productcatalog.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductRepository productRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public void importAll() {
        importFromCsv("incompatible_feeds_products.csv");
        importFromJson("incompatible_feeds_products.json");
    }

    private void importFromCsv(String resourceName) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new IllegalStateException("CSV resource not found: " + resourceName);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                int lineNumber = 0;

                Map<String, Product> productsBySku = new LinkedHashMap<>();

                line = reader.readLine();
                lineNumber++;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    if (line.isBlank()) {
                        continue;
                    }

                    String[] columns = line.split(",", -1);
                    if (columns.length < 5) {
                        System.out.println("Skipping invalid CSV line " + lineNumber + ": " + line);
                        continue;
                    }

                    Product product = mapCsvRowToProduct(columns);

                    Product previous = productsBySku.put(product.getSku(), product);
                    if (previous != null) {
                        System.out.println("Duplicate SKU in CSV, keeping last: " + product.getSku());
                    }
                }

                productRepository.saveAll(productsBySku.values());
                System.out.println("Imported " + productsBySku.size() + " unique products from CSV.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import CSV: " + resourceName, e);
        }
    }

    private Product mapCsvRowToProduct(String[] columns) {
        String rawSku = columns[0].trim();
        String name = columns[1].trim();
        String grossPriceStr = columns[2].trim();
        String stockStr = columns[3].trim();
        String brand = columns[4].trim();

        String normalizedSku = normalizeSku(rawSku);

        String normalizedPrice = grossPriceStr.replace(" ", "");
        BigDecimal finalPriceHuf = new BigDecimal(normalizedPrice);

        Integer stock = null;
        if (!stockStr.isBlank()) {
            stock = Integer.parseInt(stockStr);
        }

        boolean valid = true;
        List<String> errors = new ArrayList<>();

        if (name.isBlank()) {
            valid = false;
            errors.add("MISSING_NAME");
        }
        if (normalizedSku.isBlank()) {
            valid = false;
            errors.add("MISSING_SKU");
        }
        if (finalPriceHuf.compareTo(BigDecimal.ZERO) <= 0) {
            valid = false;
            errors.add("NON_POSITIVE_PRICE");
        }
        if (stock != null && stock < 0) {
            valid = false;
            errors.add("NEGATIVE_STOCK");
        }

        return Product.builder()
                .sku(normalizedSku)
                .name(name)
                .manufacturer(brand)
                .finalPriceHuf(finalPriceHuf)
                .stock(stock)
                .ean(null)
                .updatedAt(Instant.now())
                .source("CSV")
                .valid(valid)
                .validationErrors(errors)
                .build();
    }

    private void importFromJson(String resourceName) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new IllegalStateException("JSON resource not found: " + resourceName);
            }

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(is);
            if (!root.isArray()) {
                throw new IllegalStateException("JSON root is not an array: " + resourceName);
            }

            int count = 0;

            for (com.fasterxml.jackson.databind.JsonNode node : root) {
                String rawSku = node.path("id").asText(null);
                String normalizedSku = normalizeSku(rawSku);

                String name = node.path("name").asText("");
                String manufacturer = node.path("manufacturer").isNull() ? null : node.path("manufacturer").asText();
                String ean = node.path("ean").isNull() ? null : node.path("ean").asText();

                java.math.BigDecimal finalPriceHuf = null;
                if (node.hasNonNull("netPrice") && node.hasNonNull("vatRate")) {
                    java.math.BigDecimal net = node.get("netPrice").decimalValue();
                    java.math.BigDecimal vatRate = node.get("vatRate").decimalValue(); // pl. 0.27
                    finalPriceHuf = net.multiply(java.math.BigDecimal.ONE.add(vatRate))
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                }

                Integer stock = null;
                if (node.hasNonNull("quantityAvailable")) {
                    stock = node.get("quantityAvailable").asInt();
                }

                java.time.Instant updatedAt;
                if (node.hasNonNull("updatedAt")) {
                    updatedAt = java.time.Instant.parse(node.get("updatedAt").asText());
                } else {
                    updatedAt = java.time.Instant.now();
                }

                boolean valid = true;
                java.util.List<String> errors = new java.util.ArrayList<>();

                if (normalizedSku.isBlank()) {
                    valid = false;
                    errors.add("MISSING_SKU");
                }
                if (name.isBlank()) {
                    valid = false;
                    errors.add("MISSING_NAME");
                }
                if (finalPriceHuf == null || finalPriceHuf.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    valid = false;
                    errors.add("MISSING_OR_INVALID_PRICE");
                }
                if (stock != null && stock < 0) {
                    valid = false;
                    errors.add("NEGATIVE_STOCK");
                }

                Product product = productRepository.findBySku(normalizedSku)
                        .orElseGet(() -> Product.builder().sku(normalizedSku).build());

                product.setName(name);
                product.setManufacturer(manufacturer != null ? manufacturer : product.getManufacturer());
                product.setFinalPriceHuf(finalPriceHuf != null ? finalPriceHuf : product.getFinalPriceHuf());
                product.setStock(stock != null ? stock : product.getStock());
                product.setEan(ean != null ? ean : product.getEan());
                product.setUpdatedAt(updatedAt);
                product.setSource("JSON");
                product.setValid(valid);
                product.setValidationErrors(errors);

                if (product.getFinalPriceHuf() == null) {
                    System.out.println("Skipping JSON product without price, SKU=" + normalizedSku);
                    continue;
                }

                productRepository.save(product);
                count++;
            }

            System.out.println("Imported/merged " + count + " products from JSON.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to import JSON: " + resourceName, e);
        }
    }

    private String normalizeSku(String rawSku) {
        if (rawSku == null) {
            return "";
        }
        return rawSku.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

}

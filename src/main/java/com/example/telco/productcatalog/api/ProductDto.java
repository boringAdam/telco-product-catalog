package com.example.telco.productcatalog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductDto {

    String sku;
    String name;
    String manufacturer;
    BigDecimal finalPriceHuf;
    Integer stock;
    String ean;
    Instant updatedAt;
    String source;
    boolean valid;
    List<String> validationErrors;
}

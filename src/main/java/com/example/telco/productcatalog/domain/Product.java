package com.example.telco.productcatalog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String manufacturer;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPriceHuf;

    private Integer stock;

    private String ean;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private boolean valid;

    @ElementCollection
    @CollectionTable(name = "product_validation_errors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "error_code")
    private List<String> validationErrors;
}

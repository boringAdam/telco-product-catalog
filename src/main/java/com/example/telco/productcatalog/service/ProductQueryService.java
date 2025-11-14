package com.example.telco.productcatalog.service;

import com.example.telco.productcatalog.api.ProductDto;
import com.example.telco.productcatalog.domain.Product;
import com.example.telco.productcatalog.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    public List<ProductDto> getProducts(String filter, String sort, boolean onlyValid) {
        List<Product> products = productRepository.findAll();

        if (onlyValid) {
            products = products.stream()
                    .filter(Product::isValid)
                    .collect(Collectors.toList());
        }

        if (filter != null && !filter.isBlank()) {
            String f = filter.toLowerCase(Locale.ROOT);
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(f)) ||
                            (p.getSku() != null && p.getSku().toLowerCase(Locale.ROOT).contains(f)) ||
                            (p.getManufacturer() != null && p.getManufacturer().toLowerCase(Locale.ROOT).contains(f)) ||
                            (p.getEan() != null && p.getEan().toLowerCase(Locale.ROOT).contains(f)))
                    .collect(Collectors.toList());
        }

        Comparator<Product> comparator = buildComparator(sort);
        products = products.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Comparator<Product> buildComparator(String sort) {
        String field = "name";
        String dir = "asc";

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            field = parts[0].trim();
            if (parts.length > 1) {
                dir = parts[1].trim().toLowerCase(Locale.ROOT);
            }
        }

        Comparator<Product> base;
        switch (field) {
            case "price":
                base = Comparator.comparing(Product::getFinalPriceHuf,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "updatedAt":
                base = Comparator.comparing(Product::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "sku":
                base = Comparator.comparing(Product::getSku,
                        Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                base = Comparator.comparing(Product::getName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
        }

        if ("desc".equals(dir)) {
            base = base.reversed();
        }

        return base;
    }

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .sku(p.getSku())
                .name(p.getName())
                .manufacturer(p.getManufacturer())
                .finalPriceHuf(p.getFinalPriceHuf())
                .stock(p.getStock())
                .ean(p.getEan())
                .updatedAt(p.getUpdatedAt())
                .source(p.getSource())
                .valid(p.isValid())
                .validationErrors(p.getValidationErrors())
                .build();
    }
}

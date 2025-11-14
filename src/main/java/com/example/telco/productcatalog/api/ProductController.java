package com.example.telco.productcatalog.api;

import com.example.telco.productcatalog.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public List<ProductDto> getProducts(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "onlyValid", required = false, defaultValue = "true") boolean onlyValid) {
        return productQueryService.getProducts(filter, sort, onlyValid);
    }
}

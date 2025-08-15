package com.product.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public List<Map<String, Object>> list() {
        return List.of(
                Map.of("sku", "SKU-1", "name", "Demo Product 1", "price", 10.0),
                Map.of("sku", "SKU-2", "name", "Demo Product 2", "price", 20.0)
        );
    }
}

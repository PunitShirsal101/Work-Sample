package com.product.api;

import com.product.api.dto.PageResponse;
import com.product.api.dto.ProductRequest;
import com.product.api.dto.ProductResponse;
import com.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final String PRODUCT_NOT_FOUND = "Product not found";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid ProductRequest request) {
        try {
            ProductResponse created = productService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable UUID id) {
        Optional<ProductResponse> resp = productService.get(id);
        return resp
                .map(ResponseEntity::<Object>ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(PRODUCT_NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable UUID id, @RequestBody @Valid ProductRequest request) {
        try {
            Optional<ProductResponse> updated = productService.update(id, request);
            return updated
                    .map(ResponseEntity::<Object>ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(PRODUCT_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable UUID id) {
        boolean deleted = productService.delete(id);
        return deleted ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.status(HttpStatus.NOT_FOUND).body(PRODUCT_NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        com.product.api.dto.ProductListCriteria criteria = new com.product.api.dto.ProductListCriteria();
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir);
        criteria.setQ(q);
        criteria.setCategory(category);
        criteria.setTag(tag);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        PageResponse<ProductResponse> result = productService.list(criteria);
        return ResponseEntity.ok(result);
    }
}

package com.product.service;

import com.product.api.dto.PageResponse;
import com.product.api.dto.ProductListCriteria;
import com.product.api.dto.ProductRequest;
import com.product.api.dto.ProductResponse;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    Optional<ProductResponse> get(UUID id);
    Optional<ProductResponse> update(UUID id, ProductRequest request);
    boolean delete(UUID id);

    PageResponse<ProductResponse> list(ProductListCriteria criteria);
}
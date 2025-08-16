package com.product.service.impl;

import com.product.api.dto.PageResponse;
import com.product.api.dto.ProductRequest;
import com.product.api.dto.ProductResponse;
import com.product.domain.Product;
import com.product.integration.InventoryClient;
import com.product.messaging.ProductEventPublisher;
import com.product.messaging.events.ProductEvent;
import com.product.repository.ProductRepository;
import com.product.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final String ERR_SKU_EXISTS = "SKU already exists";

    private final ProductRepository repository;
    private final InventoryClient inventoryClient;
    private final ProductEventPublisher eventPublisher;

    public ProductServiceImpl(ProductRepository repository, InventoryClient inventoryClient, ProductEventPublisher eventPublisher) {
        this.repository = repository;
        this.inventoryClient = inventoryClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @CacheEvict(cacheNames = {"productsById", "productList"}, allEntries = true)
    public ProductResponse create(ProductRequest request) {
        // Unique SKU validation
        repository.findBySku(request.getSku()).ifPresent(p -> {
            throw new IllegalArgumentException(ERR_SKU_EXISTS);
        });
        Product p = toEntity(new Product(), request);
        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        repository.save(p);
        // publish event
        eventPublisher.publish(ProductEvent.of(ProductEvent.Type.CREATED, p.getId(), p.getSku()));
        return toResponse(p);
    }

    @Override
    @Cacheable(cacheNames = "productsById", key = "#id")
    public Optional<ProductResponse> get(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    @Override
    @CacheEvict(cacheNames = {"productsById", "productList"}, allEntries = true)
    public Optional<ProductResponse> update(UUID id, ProductRequest request) {
        Optional<Product> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        Product existing = existingOpt.get();
        // If SKU changed, ensure uniqueness
        if (!Objects.equals(existing.getSku(), request.getSku())) {
            repository.findBySku(request.getSku()).ifPresent(p -> {
                throw new IllegalArgumentException(ERR_SKU_EXISTS);
            });
        }
        Product updated = toEntity(existing, request);
        updated.setUpdatedAt(Instant.now());
        repository.save(updated);
        // publish event
        eventPublisher.publish(ProductEvent.of(ProductEvent.Type.UPDATED, updated.getId(), updated.getSku()));
        return Optional.of(toResponse(updated));
    }

    @Override
    @CacheEvict(cacheNames = {"productsById", "productList"}, allEntries = true)
    public boolean delete(UUID id) {
        Optional<Product> existing = repository.findById(id);
        existing.ifPresent(p -> {
            repository.deleteById(id);
            eventPublisher.publish(ProductEvent.of(ProductEvent.Type.DELETED, p.getId(), p.getSku()));
        });
        return existing.isPresent();
    }

    @Override
    @Cacheable(cacheNames = "productList", key = "T(java.util.Objects).hash(#criteria.page,#criteria.size,#criteria.sortBy,#criteria.sortDir,#criteria.q,#criteria.category,#criteria.tag,#criteria.minPrice,#criteria.maxPrice)")
    public PageResponse<ProductResponse> list(com.product.api.dto.ProductListCriteria criteria) {
        List<Product> all = repository.findAll();
        final String qLower = normalize(criteria.getQ());

        // Filtering
        List<Product> filtered = all.stream()
                .filter(p -> matchesQuery(p, qLower))
                .filter(p -> matchesCategory(p, criteria.getCategory()))
                .filter(p -> matchesTag(p, criteria.getTag()))
                .filter(p -> priceGte(p, criteria.getMinPrice()))
                .filter(p -> priceLte(p, criteria.getMaxPrice()))
                .collect(Collectors.toList());

        // Sorting
        Comparator<Product> comparator = buildComparator(criteria.getSortBy(), criteria.getSortDir());
        filtered.sort(comparator);

        // Pagination
        List<Product> pageItems = paginate(filtered, criteria.getPage(), criteria.getSize());

        List<ProductResponse> content = pageItems.stream().map(this::toResponse).toList();
        return new PageResponse<>(content, criteria.getPage(), criteria.getSize(), filtered.size());
    }

    private String normalize(String text) {
        if (text == null) return null;
        String t = text.trim();
        return t.isEmpty() ? null : t.toLowerCase(java.util.Locale.ROOT);
    }

    private boolean matchesQuery(Product p, String qLower) {
        if (qLower == null) return true;
        String name = p.getName();
        String desc = p.getDescription();
        return containsIgnoreCase(name, qLower) || containsIgnoreCase(desc, qLower);
    }

    private boolean matchesCategory(Product p, String category) {
        if (category == null || category.isBlank()) return true;
        Set<String> cats = p.getCategories();
        return cats != null && cats.contains(category);
    }

    private boolean matchesTag(Product p, String tag) {
        if (tag == null || tag.isBlank()) return true;
        Set<String> t = p.getTags();
        return t != null && t.contains(tag);
    }

    private boolean priceGte(Product p, BigDecimal minPrice) {
        if (minPrice == null) return true;
        BigDecimal price = p.getPrice();
        return price != null && price.compareTo(minPrice) >= 0;
    }

    private boolean priceLte(Product p, BigDecimal maxPrice) {
        if (maxPrice == null) return true;
        BigDecimal price = p.getPrice();
        return price != null && price.compareTo(maxPrice) <= 0;
    }

    private boolean containsIgnoreCase(String source, String needleLower) {
        if (source == null || needleLower == null) return false;
        return source.toLowerCase(java.util.Locale.ROOT).contains(needleLower);
    }

    private Comparator<Product> buildComparator(String sortBy, String sortDir) {
        Comparator<Product> comparator;
        if ("price".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getPrice, Comparator.nullsLast(BigDecimal::compareTo));
        } else if ("name".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(p -> Optional.ofNullable(p.getName()).orElse(""), String.CASE_INSENSITIVE_ORDER);
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(Product::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private List<Product> paginate(List<Product> list, int page, int size) {
        int from = Math.max(0, page * size);
        if (from >= list.size()) return Collections.emptyList();
        int to = Math.min(list.size(), from + size);
        return list.subList(from, to);
    }

    private Product toEntity(Product target, ProductRequest req) {
        target.setSku(req.getSku());
        target.setName(req.getName());
        target.setDescription(req.getDescription());
        target.setPrice(req.getPrice());
        target.setCurrency(req.getCurrency());
        target.setCategories(req.getCategories() != null ? new HashSet<>(req.getCategories()) : new HashSet<>());
        target.setTags(req.getTags() != null ? new HashSet<>(req.getTags()) : new HashSet<>());
        return target;
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse resp = new ProductResponse();
        resp.setId(p.getId());
        resp.setSku(p.getSku());
        resp.setName(p.getName());
        resp.setDescription(p.getDescription());
        resp.setPrice(p.getPrice());
        resp.setCurrency(p.getCurrency());
        resp.setCategories(p.getCategories());
        resp.setTags(p.getTags());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setUpdatedAt(p.getUpdatedAt());
        // inventory: check availability for qty=1
        boolean available = p.getSku() != null && inventoryClient.isAvailable(p.getSku(), 1);
        resp.setInventoryAvailable(available);
        return resp;
    }
}
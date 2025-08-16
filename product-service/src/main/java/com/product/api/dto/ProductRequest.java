package com.product.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public class ProductRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9-]{8,20}$", message = "SKU must be 8-20 chars, uppercase letters, digits or hyphen")
    private String sku;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be ISO 4217 code")
    private String currency;

    private Set<@NotBlank String> categories;
    private Set<@NotBlank String> tags;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Set<String> getCategories() { return categories; }
    public void setCategories(Set<String> categories) { this.categories = categories; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}
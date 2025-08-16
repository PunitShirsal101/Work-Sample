package com.product.api.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductListCriteria {
    private int page;
    private int size;
    private String sortBy;
    private String sortDir;
    private String q;
    private String category;
    private String tag;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductListCriteria that = (ProductListCriteria) o;
        return page == that.page && size == that.size &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(sortDir, that.sortDir) &&
                Objects.equals(q, that.q) &&
                Objects.equals(category, that.category) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(minPrice, that.minPrice) &&
                Objects.equals(maxPrice, that.maxPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sortBy, sortDir, q, category, tag, minPrice, maxPrice);
    }
}
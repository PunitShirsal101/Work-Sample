package com.inventory.repository;

import com.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    Optional<InventoryItem> findBySku(String sku);
}
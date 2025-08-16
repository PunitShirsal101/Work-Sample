package com.inventory.api;

import com.inventory.api.dto.InventoryDeductRequest;
import com.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> check(@RequestParam String sku, @RequestParam int qty) {
        return ResponseEntity.ok(inventoryService.isAvailable(sku, qty));
    }

    @PostMapping("/deduct")
    public ResponseEntity<Boolean> deduct(@RequestBody InventoryDeductRequest request) {
        inventoryService.deduct(request);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/restore")
    public ResponseEntity<Boolean> restore(@RequestBody InventoryDeductRequest request) {
        inventoryService.restore(request);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/restock")
    public ResponseEntity<Boolean> restock(@RequestBody InventoryDeductRequest request) {
        inventoryService.restock(request);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<Boolean> bulkImport(@RequestBody InventoryDeductRequest request) {
        inventoryService.bulkImport(request);
        return ResponseEntity.ok(true);
    }
}

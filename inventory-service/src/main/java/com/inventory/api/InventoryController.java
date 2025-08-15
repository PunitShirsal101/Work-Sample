package com.inventory.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/check")
    public ResponseEntity<Boolean> check(@RequestParam String sku, @RequestParam int qty) {
        // Placeholder: always true
        return ResponseEntity.ok(true);
    }

    @PostMapping("/deduct")
    public ResponseEntity<Boolean> deduct(@RequestBody InventoryDeductRequest request) {
        // Placeholder: pretend to deduct inventory successfully
        return ResponseEntity.ok(true);
    }

    @PostMapping("/restore")
    public ResponseEntity<Boolean> restore(@RequestBody InventoryDeductRequest request) {
        // Placeholder: pretend to restore inventory successfully
        return ResponseEntity.ok(true);
    }
}

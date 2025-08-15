package com.example.inventory.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/check")
    public ResponseEntity<Boolean> check(@RequestParam String sku, @RequestParam int qty) {
        // Placeholder: always true
        return ResponseEntity.ok(true);
    }
}

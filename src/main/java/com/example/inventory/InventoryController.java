package com.example.inventory;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryRepository repo;

    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/{sku}/stock")
    public Mono<Inventory> updateStock(@PathVariable String sku, @RequestBody StockRequest req) {
        return repo.save(new Inventory(sku, req.qty()));
    }

    @GetMapping("/{sku}")
    public Mono<Inventory> get(@PathVariable String sku) {
        return repo.findById(sku);
    }

    public record StockRequest(int qty) {}
}

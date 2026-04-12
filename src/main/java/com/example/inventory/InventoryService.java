package com.example.inventory;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public Mono<Integer> reserve(String sku, int qty) {
        return repository.findById(sku)
            .flatMap(inv -> {
                if (inv.stock() >= qty) {
                    int newStock = inv.stock() - qty;
                    return repository.save(inv.withStock(newStock))
                        .thenReturn(newStock);
                } else {
                    return Mono.just(-1);
                }
            })
            .defaultIfEmpty(-2); // -2 means not found
    }
}

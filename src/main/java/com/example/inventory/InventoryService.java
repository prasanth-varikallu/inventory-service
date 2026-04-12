package com.example.inventory;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public Mono<Boolean> reserve(String sku, int qty) {
        return repository.findById(sku)
            .flatMap(inv -> {
                if (inv.stock() >= qty) {
                    return repository.save(inv.withStock(inv.stock() - qty))
                        .thenReturn(true);
                } else {
                    return Mono.just(false);
                }
            })
            .defaultIfEmpty(false);
    }
}

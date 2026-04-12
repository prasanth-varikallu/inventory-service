package com.example.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = InventoryService.class)
class InventoryServiceTest {

    @MockitoBean
    private InventoryRepository repository;

    @Autowired
    private InventoryService service;

    @Test
    void shouldReserveStockIfAvailable() {
        Inventory inv = new Inventory("sku1", 10);
        when(repository.findById("sku1")).thenReturn(Mono.just(inv));
        when(repository.save(any(Inventory.class))).thenReturn(Mono.just(new Inventory("sku1", 5)));

        service.reserve("sku1", 5)
            .as(StepVerifier::create)
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void shouldNotReserveStockIfNotEnough() {
        Inventory inv = new Inventory("sku1", 2);
        when(repository.findById("sku1")).thenReturn(Mono.just(inv));

        service.reserve("sku1", 5)
            .as(StepVerifier::create)
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void shouldReturnFalseIfSkuNotFound() {
        when(repository.findById("sku1")).thenReturn(Mono.empty());

        service.reserve("sku1", 5)
            .as(StepVerifier::create)
            .expectNext(false)
            .verifyComplete();
    }
}

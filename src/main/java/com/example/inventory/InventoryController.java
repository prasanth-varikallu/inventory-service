package com.example.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InventoryController(InventoryRepository repo, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{sku}/stock")
    public Mono<Inventory> updateStock(@PathVariable String sku, @RequestBody StockRequest req) {
        return repo.save(new Inventory(sku, req.qty()))
                .flatMap(inv -> {
                    var event = new StockUpdatedEvent("StockUpdated", inv.sku(), inv.stock());
                    try {
                        String eventJson = objectMapper.writeValueAsString(event);
                        return Mono.fromFuture(kafkaTemplate.send("inventory.v1", inv.sku(), eventJson))
                                .thenReturn(inv);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    @GetMapping("/{sku}")
    public Mono<Inventory> get(@PathVariable String sku) {
        return repo.findById(sku);
    }

    public record StockRequest(int qty) {}
    public record StockUpdatedEvent(String type, String sku, int stock) {}
}

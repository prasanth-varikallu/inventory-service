package com.example.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductCreatedConsumer {

  private final InventoryRepository repository;
  private final ObjectMapper objectMapper;

  public ProductCreatedConsumer(InventoryRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "products.v1", groupId = "inventory-group")
  public void onProductCreated(String message) {
    System.out.println("inventory-service received product created event: " + message);
    try {
      ProductCreatedEvent event = objectMapper.readValue(message, ProductCreatedEvent.class);
      if ("ProductCreated".equals(event.type())) {
        repository.findById(event.sku())
            .switchIfEmpty(repository.save(new Inventory(event.sku(), 0)))
            .subscribe(inv -> System.out.println("Inventory initialized for sku: " + inv.sku()));
      }
    } catch (JsonProcessingException e) {
      System.err.println("Error parsing product created event: " + e.getMessage());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ProductCreatedEvent(String type, String sku) {}
}

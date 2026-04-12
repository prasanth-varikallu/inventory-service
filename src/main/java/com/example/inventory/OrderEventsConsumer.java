package com.example.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrderEventsConsumer {

  private final InventoryService inventoryService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public OrderEventsConsumer(InventoryService inventoryService, 
                            KafkaTemplate<String, String> kafkaTemplate, 
                            ObjectMapper objectMapper) {
    this.inventoryService = inventoryService;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "orders.v1", groupId = "inventory")
  public void onMessage(String value) {
    System.out.println("inventory-service got event: " + value);
    try {
      OrderCreatedEvent event = objectMapper.readValue(value, OrderCreatedEvent.class);
      if ("OrderCreated".equals(event.type())) {
        processOrder(event);
      }
    } catch (JsonProcessingException e) {
      System.err.println("Failed to parse event: " + value + " error: " + e.getMessage());
    }
  }

  private void processOrder(OrderCreatedEvent event) {
    inventoryService.reserve(event.sku(), event.qty())
        .flatMap(stockLevel -> {
          boolean reserved = stockLevel >= 0;
          String outcomeType = reserved ? "InventoryReserved" : "InventoryOutOfStock";
          InventoryOutcomeEvent outcome = new InventoryOutcomeEvent(
              outcomeType,
              event.orderId(),
              event.sku(),
              event.qty(),
              stockLevel,
              Instant.now().toString()
          );
          try {
            String outcomeJson = objectMapper.writeValueAsString(outcome);
            return Mono.fromFuture(kafkaTemplate.send("inventory.v1", event.orderId(), outcomeJson));
          } catch (JsonProcessingException e) {
            return Mono.error(e);
          }
        })
        .subscribe(
            result -> System.out.println("Published outcome for order: " + event.orderId()),
            error -> System.err.println("Error processing order: " + event.orderId() + " " + error.getMessage())
        );
  }
}


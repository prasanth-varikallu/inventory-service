package com.example.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventsConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderEventsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderEventsConsumer(inventoryService, kafkaTemplate, objectMapper);
    }

    @Test
    void shouldProcessOrderAndPublishSuccess() throws JsonProcessingException {
        String event = """
            {"type":"OrderCreated","orderId":"o1","sku":"s1","qty":5}
            """;
        
        when(inventoryService.reserve("s1", 5)).thenReturn(Mono.just(true));
        when(kafkaTemplate.send(eq("inventory.v1"), eq("o1"), anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));

        consumer.onMessage(event);

        verify(inventoryService).reserve("s1", 5);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("inventory.v1"), eq("o1"), captor.capture());
        
        assertTrue(captor.getValue().contains("InventoryReserved"));
    }

    @Test
    void shouldProcessOrderAndPublishOutOfStock() throws JsonProcessingException {
        String event = """
            {"type":"OrderCreated","orderId":"o1","sku":"s1","qty":5}
            """;
        
        when(inventoryService.reserve("s1", 5)).thenReturn(Mono.just(false));
        when(kafkaTemplate.send(eq("inventory.v1"), eq("o1"), anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));

        consumer.onMessage(event);

        verify(inventoryService).reserve("s1", 5);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("inventory.v1"), eq("o1"), captor.capture());
        
        assertTrue(captor.getValue().contains("InventoryOutOfStock"));
    }
}

package com.example.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderEventsConsumer.class, ObjectMapper.class})
class OrderEventsConsumerTest {

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderEventsConsumer consumer;

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

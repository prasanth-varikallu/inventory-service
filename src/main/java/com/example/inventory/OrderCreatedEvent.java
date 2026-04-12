package com.example.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
    String type,
    String orderId,
    String sku,
    int qty,
    String ts
) {}

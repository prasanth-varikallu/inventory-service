package com.example.inventory;

public record InventoryOutcomeEvent(
    String type,
    String orderId,
    String sku,
    int qty,
    String ts
) {}

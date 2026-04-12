package com.example.inventory;

public record InventoryOutcomeEvent(
    String type,
    String orderId,
    String sku,
    int qty,
    int stock,
    String ts
) {}

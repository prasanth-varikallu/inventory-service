package com.example.inventory;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

@Document
public record Inventory(
    @Id String sku,
    @Field int stock
) {
    public Inventory withStock(int newStock) {
        return new Inventory(sku, newStock);
    }
}

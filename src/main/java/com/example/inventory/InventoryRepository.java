package com.example.inventory;

import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends ReactiveCouchbaseRepository<Inventory, String> {
}

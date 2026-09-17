package edu.cit.onting.inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    Optional<InventoryItem> getItem(String productId);
    List<InventoryItem> getAllItems();
    boolean reserve(String productId, int quantity);
    void restock(String productId, int quantity);
}
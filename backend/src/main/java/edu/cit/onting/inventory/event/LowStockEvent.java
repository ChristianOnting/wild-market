package edu.cit.onting.inventory.event;

public record LowStockEvent(String productId, String productName, int currentStock) {}
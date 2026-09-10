package edu.cit.onting.shop.dto;

import edu.cit.onting.inventory.InventoryItem;

public class OrderResponse {
    private String status;
    private String reason;
    private InventoryItem inventory;

    public OrderResponse() {}

    public OrderResponse(String status, String reason, InventoryItem inventory) {
        this.status = status;
        this.reason = reason;
        this.inventory = inventory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public InventoryItem getInventory() {
        return inventory;
    }

    public void setInventory(InventoryItem inventory) {
        this.inventory = inventory;
    }
}
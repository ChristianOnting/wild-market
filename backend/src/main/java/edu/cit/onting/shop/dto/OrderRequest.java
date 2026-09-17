package edu.cit.onting.shop.dto;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> items;

    public OrderRequest() {}

    public OrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
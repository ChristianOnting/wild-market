package edu.cit.onting.shop.dto;

public class OrderItemRequest {
    private String productId;
    private Integer quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
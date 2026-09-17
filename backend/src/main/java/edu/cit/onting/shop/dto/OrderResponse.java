package edu.cit.onting.shop.dto;

public class OrderResponse {
    private String status;
    private String reason;
    private Long orderId;

    public OrderResponse() {}

    public OrderResponse(String status, String reason, Long orderId) {
        this.status = status;
        this.reason = reason;
        this.orderId = orderId;
    }

    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public Long getOrderId() { return orderId; }
}
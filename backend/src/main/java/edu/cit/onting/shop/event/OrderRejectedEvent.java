package edu.cit.onting.shop.event;

public record OrderRejectedEvent(Long orderId, String reason) {}
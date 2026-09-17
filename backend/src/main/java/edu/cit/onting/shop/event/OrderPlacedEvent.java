package edu.cit.onting.shop.event;

public record OrderPlacedEvent(Long orderId, String summary) {}
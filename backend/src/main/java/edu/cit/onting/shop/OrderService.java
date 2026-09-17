package edu.cit.onting.shop;

import edu.cit.onting.inventory.InventoryItem;
import edu.cit.onting.inventory.InventoryService;
import edu.cit.onting.shop.dto.OrderItemRequest;
import edu.cit.onting.shop.dto.OrderRequest;
import edu.cit.onting.shop.dto.OrderResponse;
import edu.cit.onting.shop.event.OrderPlacedEvent;
import edu.cit.onting.shop.event.OrderRejectedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, InventoryService inventoryService, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        // Step 1: Pre-validate ALL items before making any inventory reservations (All-or-Nothing rule)
        for (OrderItemRequest itemReq : request.getItems()) {
            Optional<InventoryItem> itemOpt = inventoryService.getItem(itemReq.getProductId());

            if (itemOpt.isEmpty()) {
                return rejectOrder(request, "Product not found: " + itemReq.getProductId());
            }

            InventoryItem item = itemOpt.get();
            if (item.getStock() < itemReq.getQuantity()) {
                return rejectOrder(request, "Insufficient stock for product " + item.getName() + " (Requested: " + itemReq.getQuantity() + ", Available: " + item.getStock() + ")");
            }
        }

        // Step 2: All items passed validation -> Reserve inventory and assemble order
        Order order = new Order("CONFIRMED", "Order processed successfully");

        for (OrderItemRequest itemReq : request.getItems()) {
            inventoryService.reserve(itemReq.getProductId(), itemReq.getQuantity());
            order.addItem(new OrderItem(itemReq.getProductId(), itemReq.getQuantity()));
        }

        Order savedOrder = orderRepository.save(order);

        // Step 3: Publish domain event (Notification module listens to this)
        eventPublisher.publishEvent(new OrderPlacedEvent(savedOrder.getOrderId(), "Order #" + savedOrder.getOrderId() + " confirmed"));

        return new OrderResponse("CONFIRMED", "Order processed successfully", savedOrder.getOrderId());
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Order is already CANCELLED");
        }

        // Return stock for each line item in the order
        for (OrderItem item : order.getItems()) {
            inventoryService.restock(item.getProductId(), item.getQuantity());
        }

        order.setStatus("CANCELLED");
        order.setReason("Order cancelled by customer");

        return orderRepository.save(order);
    }

    private OrderResponse rejectOrder(OrderRequest request, String reason) {
        Order rejectedOrder = new Order("REJECTED", reason);
        for (OrderItemRequest itemReq : request.getItems()) {
            rejectedOrder.addItem(new OrderItem(itemReq.getProductId(), itemReq.getQuantity()));
        }

        Order savedOrder = orderRepository.save(rejectedOrder);

        // Publish rejection event
        eventPublisher.publishEvent(new OrderRejectedEvent(savedOrder.getOrderId(), reason));

        return new OrderResponse("REJECTED", reason, savedOrder.getOrderId());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
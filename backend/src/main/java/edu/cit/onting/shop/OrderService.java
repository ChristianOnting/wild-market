package edu.cit.onting.shop;

import edu.cit.onting.inventory.InventoryItem;
import edu.cit.onting.inventory.InventoryService;
import edu.cit.onting.shop.dto.OrderRequest;
import edu.cit.onting.shop.dto.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService; // Injection of Interface only

    public OrderService(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    public OrderResponse placeOrder(OrderRequest request) {
        Optional<InventoryItem> itemOpt = inventoryService.getItem(request.getProductId());

        if (itemOpt.isEmpty()) {
            Order failedOrder = new Order(request.getProductId(), request.getQuantity(), "REJECTED", "Product not found");
            orderRepository.save(failedOrder);
            return new OrderResponse("REJECTED", "Product not found", null);
        }

        InventoryItem item = itemOpt.get();

        boolean reserved = inventoryService.reserve(request.getProductId(), request.getQuantity());

        if (reserved) {
            Order confirmedOrder = new Order(request.getProductId(), request.getQuantity(), "CONFIRMED", "Order processed successfully");
            orderRepository.save(confirmedOrder);

            // Re-fetch updated item with new stock level
            InventoryItem updatedItem = inventoryService.getItem(request.getProductId()).orElse(item);
            return new OrderResponse("CONFIRMED", "Order processed successfully", updatedItem);
        } else {
            Order rejectedOrder = new Order(request.getProductId(), request.getQuantity(), "REJECTED", "Requested quantity exceeds available stock (" + item.getStock() + ")");
            orderRepository.save(rejectedOrder);
            return new OrderResponse("REJECTED", "Requested quantity exceeds available stock (" + item.getStock() + ")", item);
        }
    }
}
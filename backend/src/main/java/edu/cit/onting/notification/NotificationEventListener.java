package edu.cit.onting.notification;

import edu.cit.onting.inventory.event.LowStockEvent;
import edu.cit.onting.shop.event.OrderPlacedEvent;
import edu.cit.onting.shop.event.OrderRejectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        notificationRepository.save(new Notification("ORDER CONFIRMED: " + event.summary()));
    }

    @EventListener
    public void handleOrderRejected(OrderRejectedEvent event) {
        notificationRepository.save(new Notification("ORDER REJECTED: Order #" + event.orderId() + " failed - " + event.reason()));
    }

    @EventListener
    public void handleLowStock(LowStockEvent event) {
        notificationRepository.save(new Notification("REORDER NEEDED: Low stock alert for " + event.productName() + " (" + event.productId() + "). Remaining stock: " + event.currentStock()));
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
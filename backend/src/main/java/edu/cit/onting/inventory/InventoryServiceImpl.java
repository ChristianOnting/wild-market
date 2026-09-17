package edu.cit.onting.inventory;

import edu.cit.onting.inventory.event.LowStockEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class InventoryServiceImpl implements InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    InventoryServiceImpl(InventoryRepository inventoryRepository, ApplicationEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<InventoryItem> getItem(String productId) {
        return inventoryRepository.findById(productId);
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public boolean reserve(String productId, int quantity) {
        Optional<InventoryItem> optionalItem = inventoryRepository.findById(productId);
        if (optionalItem.isEmpty()) {
            return false;
        }

        InventoryItem item = optionalItem.get();
        if (item.getStock() < quantity) {
            return false;
        }

        item.setStock(item.getStock() - quantity);
        inventoryRepository.save(item);

        // Check low stock threshold rule
        if (item.getStock() < LOW_STOCK_THRESHOLD) {
            eventPublisher.publishEvent(new LowStockEvent(item.getProductId(), item.getName(), item.getStock()));
        }

        return true;
    }

    @Override
    @Transactional
    public void restock(String productId, int quantity) {
        inventoryRepository.findById(productId).ifPresent(item -> {
            item.setStock(item.getStock() + quantity);
            inventoryRepository.save(item);
        });
    }
}
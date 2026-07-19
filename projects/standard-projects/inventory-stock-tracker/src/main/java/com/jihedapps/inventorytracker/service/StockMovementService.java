package com.jihedapps.inventorytracker.service;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.entity.MovementType;
import com.jihedapps.inventorytracker.entity.StockMovement;
import com.jihedapps.inventorytracker.exception.InsufficientStockException;
import com.jihedapps.inventorytracker.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ItemService itemService;

    public StockMovementService(StockMovementRepository stockMovementRepository, ItemService itemService) {
        this.stockMovementRepository = stockMovementRepository;
        this.itemService = itemService;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findByItem(Long itemId) {
        return stockMovementRepository.findByItemIdOrderByTimestampDesc(itemId);
    }

    // La seule facon d'entrer/sortir du stock : ca met a jour Item.quantity dans la meme
    // transaction que l'ecriture du mouvement, pour que les deux ne divergent jamais.
    public StockMovement record(Long itemId, MovementType type, int quantity, String reason) {
        Item item = itemService.findById(itemId);

        if (type == MovementType.OUT && quantity > item.getQuantity()) {
            throw new InsufficientStockException(
                    "Stock insuffisant pour " + item.getSku() + " : demande " + quantity
                            + ", disponible " + item.getQuantity());
        }

        item.setQuantity(type == MovementType.IN
                ? item.getQuantity() + quantity
                : item.getQuantity() - quantity);

        StockMovement movement = new StockMovement(item, type, quantity, reason);
        return stockMovementRepository.save(movement);
    }
}

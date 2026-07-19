package com.jihedapps.inventorytracker.controller;

import com.jihedapps.inventorytracker.entity.MovementType;
import com.jihedapps.inventorytracker.entity.StockMovement;
import com.jihedapps.inventorytracker.service.StockMovementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items/{itemId}/movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public List<StockMovement> findByItem(@PathVariable Long itemId) {
        return stockMovementService.findByItem(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovement record(@PathVariable Long itemId, @RequestBody Map<String, Object> body) {
        MovementType type = MovementType.valueOf((String) body.get("type"));
        int quantity = ((Number) body.get("quantity")).intValue();
        String reason = (String) body.get("reason");
        return stockMovementService.record(itemId, type, quantity, reason);
    }
}

package com.jihedapps.inventorytracker.controller;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.entity.MovementType;
import com.jihedapps.inventorytracker.service.ItemService;
import com.jihedapps.inventorytracker.service.StockMovementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InventoryBoardController {

    private final ItemService itemService;
    private final StockMovementService stockMovementService;

    public InventoryBoardController(ItemService itemService, StockMovementService stockMovementService) {
        this.itemService = itemService;
        this.stockMovementService = stockMovementService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("items", itemService.findAll());
        return "items";
    }

    @GetMapping("/items/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Item item = itemService.findById(id);
        model.addAttribute("item", item);
        model.addAttribute("movements", stockMovementService.findByItem(id));
        model.addAttribute("types", MovementType.values());
        return "item-detail";
    }

    @PostMapping("/items/{id}/movements")
    public String recordMovement(@PathVariable Long id,
                                  @RequestParam MovementType type,
                                  @RequestParam int quantity,
                                  @RequestParam(required = false) String reason) {
        stockMovementService.record(id, type, quantity, reason);
        return "redirect:/items/" + id;
    }
}

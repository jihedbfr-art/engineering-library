package com.jihedapps.inventorytracker.controller;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> findAll(@RequestParam(required = false) Boolean lowStock) {
        return Boolean.TRUE.equals(lowStock) ? itemService.findLowStock() : itemService.findAll();
    }

    @GetMapping("/{id}")
    public Item findById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item create(@Valid @RequestBody Item item) {
        return itemService.create(item);
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Long id, @Valid @RequestBody Item item) {
        return itemService.update(id, item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}

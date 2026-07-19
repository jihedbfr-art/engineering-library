package com.jihedapps.inventorytracker.service;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.exception.ResourceNotFoundException;
import com.jihedapps.inventorytracker.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Item> findLowStock() {
        return itemRepository.findLowStock();
    }

    public Item create(Item payload) {
        itemRepository.findBySku(payload.getSku()).ifPresent(existing -> {
            throw new IllegalArgumentException("SKU deja utilise : " + payload.getSku());
        });
        payload.setId(null);
        return itemRepository.save(payload);
    }

    public Item update(Long id, Item payload) {
        Item existing = findById(id);
        existing.setName(payload.getName());
        existing.setCategory(payload.getCategory());
        existing.setReorderThreshold(payload.getReorderThreshold());
        return itemRepository.save(existing);
    }

    public void delete(Long id) {
        Item existing = findById(id);
        itemRepository.delete(existing);
    }
}

package com.jihedapps.inventorytracker.repository;

import com.jihedapps.inventorytracker.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findBySku(String sku);

    List<Item> findByCategory(String category);

    @Query("select i from Item i where i.quantity <= i.reorderThreshold")
    List<Item> findLowStock();
}

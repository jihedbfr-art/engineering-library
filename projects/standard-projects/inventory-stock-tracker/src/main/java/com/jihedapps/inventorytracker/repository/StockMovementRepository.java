package com.jihedapps.inventorytracker.repository;

import com.jihedapps.inventorytracker.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByItemIdOrderByTimestampDesc(Long itemId);
}

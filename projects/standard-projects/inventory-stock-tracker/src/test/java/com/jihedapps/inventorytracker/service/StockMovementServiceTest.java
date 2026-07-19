package com.jihedapps.inventorytracker.service;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.entity.MovementType;
import com.jihedapps.inventorytracker.entity.StockMovement;
import com.jihedapps.inventorytracker.exception.InsufficientStockException;
import com.jihedapps.inventorytracker.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ItemService itemService;

    private StockMovementService stockMovementService;

    @BeforeEach
    void setUp() {
        stockMovementService = new StockMovementService(stockMovementRepository, itemService);
    }

    private Item item(int quantity) {
        Item item = new Item("SKU-1", "Cable HDMI", "cables", quantity, 5);
        item.setId(1L);
        return item;
    }

    @Test
    void recordInIncreasesQuantity() {
        Item item = item(10);
        when(itemService.findById(1L)).thenReturn(item);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementService.record(1L, MovementType.IN, 5, "reappro fournisseur");

        assertThat(item.getQuantity()).isEqualTo(15);
    }

    @Test
    void recordOutDecreasesQuantity() {
        Item item = item(10);
        when(itemService.findById(1L)).thenReturn(item);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementService.record(1L, MovementType.OUT, 4, "commande client");

        assertThat(item.getQuantity()).isEqualTo(6);
    }

    @Test
    void recordOutRejectsWhenNotEnoughStock() {
        Item item = item(3);
        when(itemService.findById(1L)).thenReturn(item);

        assertThatThrownBy(() -> stockMovementService.record(1L, MovementType.OUT, 10, "commande client"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("SKU-1");

        // la quantite ne doit pas bouger si le mouvement est rejete
        assertThat(item.getQuantity()).isEqualTo(3);
    }
}

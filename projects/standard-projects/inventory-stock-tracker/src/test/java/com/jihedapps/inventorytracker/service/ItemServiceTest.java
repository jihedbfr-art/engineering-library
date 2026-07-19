package com.jihedapps.inventorytracker.service;

import com.jihedapps.inventorytracker.entity.Item;
import com.jihedapps.inventorytracker.exception.ResourceNotFoundException;
import com.jihedapps.inventorytracker.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createRejectsDuplicateSku() {
        Item existing = new Item("SKU-1", "Cable HDMI", "cables", 10, 2);
        when(itemRepository.findBySku("SKU-1")).thenReturn(Optional.of(existing));

        Item payload = new Item("SKU-1", "Cable HDMI 2m", "cables", 5, 1);

        assertThatThrownBy(() -> itemService.create(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU-1");
    }

    @Test
    void createClearsIncomingIdAndSaves() {
        when(itemRepository.findBySku("SKU-2")).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item payload = new Item("SKU-2", "Souris USB", "peripheriques", 20, 5);
        payload.setId(999L);

        Item result = itemService.create(payload);

        assertThat(result.getId()).isNull();
        assertThat(result.getSku()).isEqualTo("SKU-2");
    }

    @Test
    void updateOverwritesFieldsButKeepsQuantity() {
        Item existing = new Item("SKU-3", "Clavier", "peripheriques", 15, 3);
        existing.setId(3L);
        when(itemRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item payload = new Item("SKU-3", "Clavier mecanique", "peripheriques-premium", 0, 5);

        Item result = itemService.update(3L, payload);

        assertThat(result.getName()).isEqualTo("Clavier mecanique");
        assertThat(result.getCategory()).isEqualTo("peripheriques-premium");
        assertThat(result.getReorderThreshold()).isEqualTo(5);
        // la quantite ne se modifie que via un mouvement de stock, jamais via update()
        assertThat(result.getQuantity()).isEqualTo(15);
    }

    @Test
    void findLowStockDelegatesToRepository() {
        when(itemRepository.findLowStock()).thenReturn(List.of());

        List<Item> result = itemService.findLowStock();

        assertThat(result).isEmpty();
        verify(itemRepository).findLowStock();
    }

    @Test
    void deleteRemovesExistingItem() {
        Item existing = new Item("SKU-4", "Ecran", "peripheriques", 2, 1);
        existing.setId(4L);
        when(itemRepository.findById(4L)).thenReturn(Optional.of(existing));

        itemService.delete(4L);

        verify(itemRepository).delete(existing);
    }
}

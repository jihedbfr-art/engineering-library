package com.jihedapps.inventorytracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    private String category;

    @Min(0)
    private int quantity = 0;

    @Min(0)
    private int reorderThreshold = 0;

    public Item() {
    }

    public Item(String sku, String name, String category, int quantity, int reorderThreshold) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public boolean isLowStock() {
        return quantity <= reorderThreshold;
    }
}

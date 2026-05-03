package com.hangrong.vendor;

import org.bukkit.inventory.ItemStack;

public class VendorItem {

    private final String id;
    private ItemStack itemStack;
    private double price;
    private int stock;
    private String displayName;

    public VendorItem(String id, ItemStack itemStack, double price, int stock) {
        this.id = id;
        this.itemStack = itemStack.clone();
        this.price = price;
        this.stock = stock;
        this.displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
                ? itemStack.getItemMeta().getDisplayName()
                : formatItemName(itemStack.getType().name());
    }

    private String formatItemName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(0, stock);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean hasStock() {
        return stock > 0;
    }

    public void addStock(int amount) {
        this.stock += amount;
    }

    public void removeStock(int amount) {
        this.stock = Math.max(0, this.stock - amount);
    }
}

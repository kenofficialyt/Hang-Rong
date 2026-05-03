package com.hangrong.vendor;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Vendor {

    private final String id;
    private final String owner;
    private String npcId;
    private Location location;
    private final Map<String, VendorItem> items;
    private long lastActivity;
    private Double customTax;

    public Vendor(String id, String owner, Location location, String npcId) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.npcId = npcId;
        this.items = new LinkedHashMap<>();
        this.lastActivity = System.currentTimeMillis();
        this.customTax = null;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Map<String, VendorItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    public Double getCustomTax() {
        return customTax;
    }

    public void setCustomTax(Double customTax) {
        this.customTax = customTax;
    }

    public boolean addItem(VendorItem item) {
        if (items.containsKey(item.getId())) return false;
        items.put(item.getId(), item);
        updateActivity();
        return true;
    }

    public boolean removeItem(String itemId) {
        VendorItem removed = items.remove(itemId);
        if (removed != null) updateActivity();
        return removed != null;
    }

    public VendorItem getItem(String itemId) {
        return items.get(itemId);
    }

    public boolean hasItem(String itemId) {
        return items.containsKey(itemId);
    }

    public int getItemCount() {
        return items.size();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("owner", owner);
        data.put("npcId", npcId);

        Map<String, Object> locData = new HashMap<>();
        locData.put("world", location.getWorld().getName());
        locData.put("x", location.getX());
        locData.put("y", location.getY());
        locData.put("z", location.getZ());
        locData.put("yaw", location.getYaw());
        locData.put("pitch", location.getPitch());
        data.put("location", locData);

        Map<String, Object> itemsData = new HashMap<>();
        for (Map.Entry<String, VendorItem> entry : items.entrySet()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("item", entry.getValue().getItemStack().serialize());
            itemData.put("price", entry.getValue().getPrice());
            itemData.put("stock", entry.getValue().getStock());
            itemData.put("displayName", entry.getValue().getDisplayName());
            itemsData.put(entry.getKey(), itemData);
        }
        data.put("items", itemsData);
        data.put("lastActivity", lastActivity);
        if (customTax != null) {
            data.put("customTax", customTax);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Vendor deserialize(Map<String, Object> data) {
        String id = (String) data.get("id");
        String owner = (String) data.get("owner");
        String npcId = (String) data.get("npcId");

        Map<String, Object> locData = (Map<String, Object>) data.get("location");
        Location location = new Location(
                Bukkit.getWorld((String) locData.get("world")),
                ((Number) locData.get("x")).doubleValue(),
                ((Number) locData.get("y")).doubleValue(),
                ((Number) locData.get("z")).doubleValue(),
                ((Number) locData.get("yaw")).floatValue(),
                ((Number) locData.get("pitch")).floatValue()
        );

        Vendor vendor = new Vendor(id, owner, location, npcId);

        Map<String, Object> itemsData = (Map<String, Object>) data.get("items");
        if (itemsData != null) {
            for (Map.Entry<String, Object> entry : itemsData.entrySet()) {
                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                org.bukkit.inventory.ItemStack itemStack = org.bukkit.inventory.ItemStack.deserialize(
                        (Map<String, Object>) itemData.get("item"));
                double price = ((Number) itemData.get("price")).doubleValue();
                int stock = ((Number) itemData.get("stock")).intValue();
                String displayName = (String) itemData.get("displayName");

                VendorItem vendorItem = new VendorItem(entry.getKey(), itemStack, price, stock);
                vendorItem.setDisplayName(displayName);
                vendor.items.put(entry.getKey(), vendorItem);
            }
        }

        vendor.lastActivity = ((Number) data.get("lastActivity")).longValue();
        if (data.containsKey("customTax")) {
            vendor.customTax = ((Number) data.get("customTax")).doubleValue();
        }
        return vendor;
    }
}

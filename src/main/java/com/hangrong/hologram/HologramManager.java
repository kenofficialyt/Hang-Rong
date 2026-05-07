package com.hangrong.hologram;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import com.hangrong.vendor.VendorItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final HangRong plugin;
    private final Map<String, List<Entity>> vendorHolograms;
    private final Map<String, Integer> rotationIndices;
    private final NamespacedKey HOLOGRAM_KEY;

    public HologramManager(HangRong plugin) {
        this.plugin = plugin;
        this.vendorHolograms = new ConcurrentHashMap<>();
        this.rotationIndices = new ConcurrentHashMap<>();
        this.HOLOGRAM_KEY = new NamespacedKey(plugin, "hologram_owner");

        if (plugin.getConfigManager().getBoolean("npc.hologram-enabled")) {
            startHologramUpdater();
        }
    }

    public void createVendorHologram(Vendor vendor) {
        if (!plugin.getConfigManager().getBoolean("npc.hologram-enabled")) return;

        removeHologram(vendor.getId());
        rotationIndices.put(vendor.getId(), 0);

        Location baseLoc = vendor.getLocation().clone();
        baseLoc.add(0, 2.8, 0);

        List<String> lines = getHologramLines(vendor, 0);
        List<Entity> hologramEntities = new ArrayList<>();

        double lineHeight = plugin.getConfigManager().getDouble("npc.hologram-line-height");

        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = baseLoc.clone();
            lineLoc.subtract(0, i * lineHeight, 0);

            ArmorStand stand = (ArmorStand) lineLoc.getWorld().spawnEntity(lineLoc, EntityType.ARMOR_STAND);
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
            stand.setCustomNameVisible(true);
            stand.setVisible(false);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.setMarker(true);
            stand.setAI(false);
            stand.setSilent(true);
            stand.getPersistentDataContainer().set(HOLOGRAM_KEY, PersistentDataType.STRING, vendor.getId());

            hologramEntities.add(stand);
        }

        vendorHolograms.put(vendor.getId(), hologramEntities);
        plugin.getLogger().info("Da tao hologram cho sáº¡p hÃ ng: " + vendor.getId());
    }

    public void removeHologram(String vendorId) {
        List<Entity> entities = vendorHolograms.remove(vendorId);
        if (entities != null) {
            for (Entity entity : entities) {
                if (!entity.isDead()) {
                    entity.remove();
                }
            }
        }
        rotationIndices.remove(vendorId);
    }

    public void updateHologram(Vendor vendor) {
        if (!plugin.getConfigManager().getBoolean("npc.hologram-enabled")) return;
        
        int totalItems = vendor.getItemCount();
        int limit = plugin.getConfigManager().getInt("npc.hologram-display-limit");
        int currentIndex = rotationIndices.getOrDefault(vendor.getId(), 0);
        
        if (totalItems > limit) {
            currentIndex = (currentIndex + 1) % totalItems;
        } else {
            currentIndex = 0;
        }

        rotationIndices.put(vendor.getId(), currentIndex);
        List<String> lines = getHologramLines(vendor, currentIndex);
        
        List<Entity> existingEntities = vendorHolograms.get(vendor.getId());
        if (existingEntities != null && !existingEntities.isEmpty()) {
            for (int i = 0; i < existingEntities.size() && i < lines.size(); i++) {
                Entity entity = existingEntities.get(i);
                entity.setCustomName(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
            }
            return;
        }

        removeHologram(vendor.getId());
        
        Location baseLoc = vendor.getLocation().clone();
        baseLoc.add(0, 2.5, 0);
        
        List<Entity> hologramEntities = new ArrayList<>();
        double lineHeight = plugin.getConfigManager().getDouble("npc.hologram-line-height");

        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = baseLoc.clone();
            lineLoc.subtract(0, i * lineHeight, 0);

            ArmorStand stand = (ArmorStand) lineLoc.getWorld().spawnEntity(lineLoc, EntityType.ARMOR_STAND);
            stand.setCustomName(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
            stand.setCustomNameVisible(true);
            stand.setVisible(false);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.setMarker(true);
            stand.setAI(false);
            stand.setSilent(true);
            stand.getPersistentDataContainer().set(HOLOGRAM_KEY, PersistentDataType.STRING, vendor.getId());
            hologramEntities.add(stand);
        }

        vendorHolograms.put(vendor.getId(), hologramEntities);
    }

    public void removeAllHolograms() {
        for (String vendorId : vendorHolograms.keySet()) {
            removeHologram(vendorId);
        }
        vendorHolograms.clear();
        rotationIndices.clear();
    }

    private List<String> getHologramLines(Vendor vendor, int startIndex) {
        List<String> lines = new ArrayList<>();
        lines.add("&6&l" + vendor.getId());

        int limit = plugin.getConfigManager().getInt("npc.hologram-display-limit");
        List<VendorItem> items = new ArrayList<>(vendor.getItems().values());
        int totalItems = items.size();

        if (totalItems == 0) {
            lines.add("&7Chua co mat hang nao!");
        } else {
            int displayCount = Math.min(limit, totalItems);
            for (int i = 0; i < displayCount; i++) {
                int itemIndex = (startIndex + i) % totalItems;
                VendorItem item = items.get(itemIndex);
                
                String stockText = item.hasStock()
                        ? "&a" + item.getStock()
                        : "&cHet hang";
                lines.add("&7" + item.getDisplayName() + ": &e" + plugin.getEconomyManager().format(item.getPrice()) + " (" + stockText + "&7)");
            }
            if (totalItems > limit) {
                lines.add("&7... +" + (totalItems - limit) + " items");
            }
        }

        return lines;
    }

    private void startHologramUpdater() {
        int interval = plugin.getConfigManager().getInt("npc.hologram-update-interval") * 20;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Vendor vendor : plugin.getVendorManager().getAllVendors().values()) {
                updateHologram(vendor);
            }
        }, interval, interval);
    }
}

package com.hangrong.npc;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Color;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NPCManager implements Listener {

    private final HangRong plugin;
    private final Map<UUID, String> entityToVendor;
    private final Map<UUID, Long> clickCooldowns;
    private final NamespacedKey VENDOR_KEY;

    public NPCManager(HangRong plugin) {
        this.plugin = plugin;
        this.entityToVendor = new ConcurrentHashMap<>();
        this.clickCooldowns = new ConcurrentHashMap<>();
        this.VENDOR_KEY = new NamespacedKey(plugin, "vendor_id");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        cleanupAllNPCs();
    }

    private void cleanupAllNPCs() {
        for (var world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(VENDOR_KEY, PersistentDataType.STRING)) {
                    entity.remove();
                }
            }
        }
        entityToVendor.clear();
        plugin.getLogger().info("Da cleanup NPCs cu!");
    }

    public void createVendorNPC(Vendor vendor) {
        Location loc = vendor.getLocation().clone();
        loc.setYaw(0);
        loc.setPitch(0);

        ArmorStand npc = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        npc.setCustomName("Â§6Â§l[ NgÆ°á»i BÃ¡n - " + vendor.getId() + " ]");
        npc.setCustomNameVisible(true);
        npc.setVisible(false);
        npc.setInvulnerable(true);
        npc.setGravity(false);
        npc.setBasePlate(false);
        npc.setAI(false);
        npc.setSilent(true);
        npc.setMarker(true);
        npc.setArms(true);
        npc.setSmall(false);
        
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        npc.getEquipment().setHelmet(head);
        
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        if (chestMeta != null) {
            chestMeta.setColor(Color.fromRGB(139, 69, 19));
            chestplate.setItemMeta(chestMeta);
        }
        npc.getEquipment().setChestplate(chestplate);
        
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta legMeta = (LeatherArmorMeta) leggings.getItemMeta();
        if (legMeta != null) {
            legMeta.setColor(Color.fromRGB(139, 69, 19));
            leggings.setItemMeta(legMeta);
        }
        npc.getEquipment().setLeggings(leggings);
        
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootMeta = (LeatherArmorMeta) boots.getItemMeta();
        if (bootMeta != null) {
            bootMeta.setColor(Color.fromRGB(50, 50, 50));
            boots.setItemMeta(bootMeta);
        }
        npc.getEquipment().setBoots(boots);
        
        npc.getPersistentDataContainer().set(VENDOR_KEY, PersistentDataType.STRING, vendor.getId());

        entityToVendor.put(npc.getUniqueId(), vendor.getId());
        plugin.getLogger().info("Da tao NPC cho sáº¡p hÃ ng: " + vendor.getId() + " (Entity: " + npc.getUniqueId() + ")");
    }

    public void removeVendorNPC(Vendor vendor) {
        String vendorId = vendor.getId();
        for (Map.Entry<UUID, String> entry : entityToVendor.entrySet()) {
            if (entry.getValue().equals(vendorId)) {
                Entity entity = Bukkit.getEntity(entry.getKey());
                if (entity != null) {
                    entity.remove();
                }
                entityToVendor.remove(entry.getKey());
                break;
            }
        }
    }

    @EventHandler
    public void onNPCClick(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) return;

        String vendorId = entity.getPersistentDataContainer().get(VENDOR_KEY, PersistentDataType.STRING);
        if (vendorId == null) return;

        Vendor vendor = plugin.getVendorManager().getVendor(vendorId);
        if (vendor == null) return;

        event.setCancelled(true);
        handleNPCClick(event.getPlayer(), vendor);
    }

    private void handleNPCClick(Player player, Vendor vendor) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldown = (long) (plugin.getConfigManager().getDouble("npc.click-cooldown") * 1000);

        Long lastClick = clickCooldowns.get(playerId);
        if (lastClick != null && (now - lastClick) < cooldown) {
            return;
        }

        clickCooldowns.put(playerId, now);
        plugin.getGuiManager().openVendorGUI(player, vendor);
    }
}

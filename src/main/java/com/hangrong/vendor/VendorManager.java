package com.hangrong.vendor;

import com.hangrong.HangRong;
import com.hangrong.transaction.TransactionManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VendorManager {

    private final HangRong plugin;
    private final Map<String, Vendor> vendors;
    private final Map<String, String> npcIdToVendor;
    private final TransactionManager transactionManager;

    public VendorManager(HangRong plugin) {
        this.plugin = plugin;
        this.vendors = new HashMap<>();
        this.npcIdToVendor = new HashMap<>();
        this.transactionManager = new TransactionManager(plugin);
    }

    public void loadVendors() {
        File vendorsFile = new File(plugin.getDataFolder(), "vendors.yml");
        if (!vendorsFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(vendorsFile);

        for (String key : config.getKeys(false)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) config.get(key);
                Vendor vendor = Vendor.deserialize(data);
                vendors.put(vendor.getId(), vendor);
                npcIdToVendor.put(vendor.getNpcId(), vendor.getId());
                
                // Spawn NPC and Hologram for loaded vendors
                plugin.getNpcManager().createVendorNPC(vendor);
                plugin.getHologramManager().createVendorHologram(vendor);
            } catch (Exception e) {
                plugin.getLogger().warning("Không thể load vendor " + key + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Da load " + vendors.size() + " sạp hàng!");
    }

    public void saveVendors() {
        File vendorsFile = new File(plugin.getDataFolder(), "vendors.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Vendor> entry : vendors.entrySet()) {
            config.set(entry.getKey(), entry.getValue().serialize());
        }

        try {
            config.save(vendorsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu vendors: " + e.getMessage());
        }

        transactionManager.saveTransactions();
    }

    public boolean createVendor(Player player, String id) {
        if (vendors.containsKey(id)) return false;

        int maxStalls = getMaxStallsPerPlayer(player);
        long count = vendors.values().stream()
                .filter(v -> v.getOwner().equals(player.getUniqueId().toString()))
                .count();
        if (count >= maxStalls) return false;

        String npcId = "hr_npc_" + UUID.randomUUID().toString().substring(0, 8);
        Vendor vendor = new Vendor(id, player.getUniqueId().toString(), player.getLocation(), npcId);
        vendors.put(id, vendor);
        npcIdToVendor.put(npcId, id);

        plugin.getNpcManager().createVendorNPC(vendor);
        plugin.getHologramManager().createVendorHologram(vendor);

        plugin.getLogger().info(player.getName() + " da tao sạp hàng: " + id);
        return true;
    }

    public boolean deleteVendor(String id) {
        Vendor vendor = vendors.remove(id);
        if (vendor == null) return false;

        npcIdToVendor.remove(vendor.getNpcId());
        plugin.getNpcManager().removeVendorNPC(vendor);
        plugin.getHologramManager().removeHologram(vendor.getId());
        transactionManager.clearTransactions(id);

        return true;
    }

    public Vendor getVendor(String id) {
        return vendors.get(id);
    }

    public Vendor getVendorByNpcId(String npcId) {
        String vendorId = npcIdToVendor.get(npcId);
        return vendorId != null ? vendors.get(vendorId) : null;
    }

    public List<Vendor> getPlayerVendors(Player player) {
        String uuid = player.getUniqueId().toString();
        List<Vendor> result = new ArrayList<>();
        for (Vendor vendor : vendors.values()) {
            if (vendor.getOwner().equals(uuid)) {
                result.add(vendor);
            }
        }
        return result;
    }

    public int getMaxStallsPerPlayer(Player player) {
        if (player.hasPermission("hangrong.max-stalls")) {
            return player.getEffectivePermissions().stream()
                    .filter(p -> p.getPermission().startsWith("hangrong.max-stalls."))
                    .map(p -> p.getPermission().substring("hangrong.max-stalls.".length()))
                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::parseInt)
                    .max(Integer::compareTo)
                    .orElse(plugin.getConfigManager().getInt("vendor.max-stalls-per-player"));
        }
        return plugin.getConfigManager().getInt("vendor.max-stalls-per-player");
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public Map<String, Vendor> getAllVendors() {
        return Collections.unmodifiableMap(vendors);
    }

    public void startAutoSave() {
        int interval = plugin.getConfigManager().getInt("vendor.auto-save-interval") * 20;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveVendors, interval, interval);
    }
}

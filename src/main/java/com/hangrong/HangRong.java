package com.hangrong;

import com.hangrong.commands.HangRongCommand;
import com.hangrong.commands.VendorTabCompleter;
import com.hangrong.config.ConfigManager;
import com.hangrong.economy.EconomyManager;
import com.hangrong.gui.GUIListener;
import com.hangrong.gui.GUIManager;
import com.hangrong.hologram.HologramManager;
import com.hangrong.npc.NPCManager;
import com.hangrong.utils.Messages;
import com.hangrong.vendor.VendorManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HangRong extends JavaPlugin {

    private static HangRong instance;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private VendorManager vendorManager;
    private NPCManager npcManager;
    private HologramManager hologramManager;
    private GUIManager guiManager;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messages = new Messages(this);
        economyManager = new EconomyManager(this);
        vendorManager = new VendorManager(this);
        npcManager = new NPCManager(this);
        hologramManager = new HologramManager(this);
        guiManager = new GUIManager(this);

        if (!economyManager.setupEconomy()) {
            getLogger().warning("Không tìm thấy economy plugin (Vault/PlayerPoints)! Một số tính năng có thể không hoạt động.");
        }

        if (Bukkit.getPluginManager().getPlugin("FancyNpcs") != null) {
            getLogger().info("FancyNpcs detected!");
        }

        if (Bukkit.getPluginManager().getPlugin("FancyHolograms") != null) {
            getLogger().info("FancyHolograms detected!");
        }

        vendorManager.loadVendors();

        getCommand("hangrong").setExecutor(new HangRongCommand(this));
        getCommand("hangrong").setTabCompleter(new VendorTabCompleter(this));

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        vendorManager.startAutoSave();

        getLogger().info("HangRong v" + getDescription().getVersion() + " đã được enable!");
    }

    @Override
    public void onDisable() {
        if (vendorManager != null) {
            vendorManager.saveVendors();
        }
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        getLogger().info("HangRong đã được disable!");
    }

    public static HangRong getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public VendorManager getVendorManager() {
        return vendorManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public Messages getMessages() {
        return messages;
    }
}

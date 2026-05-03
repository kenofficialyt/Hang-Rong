package com.hangrong.config;

import com.hangrong.HangRong;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {

    private final HangRong plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(HangRong plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadDefaults();
    }

    private void loadDefaults() {
        addDefault("economy.type", "VAULT");
        addDefault("economy.playerpoints-plugin-name", "PlayerPoints");
        addDefault("tax.enabled", true);
        addDefault("tax.percentage", 5.0);
        addDefault("tax.tax-account", "server");
        addDefault("tax.bypass-permission", "hangrong.admin.bypass-tax");
        addDefault("npc.hologram-enabled", true);
        addDefault("npc.hologram-update-interval", 5);
        addDefault("npc.hologram-line-height", 0.35);
        addDefault("npc.hologram-display-limit", 5);
        addDefault("npc.click-cooldown", 1.0);
        addDefault("npc.skin", "default");
        addDefault("gui.rows", 4);
        addDefault("gui.title", "&6&lSạp hàng của %seller%");
        addDefault("gui.fill-empty-slots", true);
        addDefault("gui.filler-item", "GRAY_STAINED_GLASS_PANE");
        addDefault("gui.filler-name", "&7");
        addDefault("gui.buy-item-name", "&a%s% &7- &e$%price%");
        addDefault("gui.out-of-stock-name", "&c%s% &7- &cHet hàng");
        addDefault("gui.purchase-confirm-title", "&aXác nhận mua");
        addDefault("gui.purchase-confirm-rows", 3);
        addDefault("bulk-pricing.enabled", true);

        if (!config.contains("bulk-pricing.discount-brackets")) {
            config.set("bulk-pricing.discount-brackets.1.amount", 8);
            config.set("bulk-pricing.discount-brackets.1.discount", 5);
            config.set("bulk-pricing.discount-brackets.2.amount", 16);
            config.set("bulk-pricing.discount-brackets.2.discount", 10);
            config.set("bulk-pricing.discount-brackets.3.amount", 32);
            config.set("bulk-pricing.discount-brackets.3.discount", 15);
            config.set("bulk-pricing.discount-brackets.4.amount", 64);
            config.set("bulk-pricing.discount-brackets.4.discount", 20);
        }

        addDefault("transaction-history.enabled", true);
        addDefault("transaction-history.max-entries-per-vendor", 100);
        addDefault("transaction-history.storage-file", "transactions");
        addDefault("transaction-history.auto-delete-days", 30);
        addDefault("vendor.max-items-per-stall", 27);
        addDefault("vendor.max-stalls-per-player", 3);
        addDefault("vendor.stall-expire-days", 7);
        addDefault("vendor.auto-save-interval", 300);
        saveConfig();
    }

    private void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu config: " + e.getMessage());
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadDefaults();
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public double getDouble(String path) {
        return config.getDouble(path, 0.0);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}

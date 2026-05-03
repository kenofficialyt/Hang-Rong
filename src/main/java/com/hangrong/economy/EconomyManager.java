package com.hangrong.economy;

import com.hangrong.HangRong;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class EconomyManager {

    private final HangRong plugin;
    private EconomyType economyType;
    private Economy vaultEconomy;
    private PlayerPointsAPI playerPointsAPI;

    public enum EconomyType {
        VAULT,
        PLAYERPOINTS
    }

    public EconomyManager(HangRong plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        String type = plugin.getConfigManager().getString("economy.type").toUpperCase();

        try {
            economyType = EconomyType.valueOf(type);
        } catch (IllegalArgumentException e) {
            economyType = EconomyType.VAULT;
        }

        switch (economyType) {
            case VAULT:
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    var registered = Bukkit.getServicesManager().getRegistration(Economy.class);
                    if (registered != null) {
                        vaultEconomy = registered.getProvider();
                        plugin.getLogger().info("Using Vault economy: " + vaultEconomy.getName());
                        return true;
                    }
                }
                plugin.getLogger().warning("Vault not found! Trying PlayerPoints...");
                return setupPlayerPoints();

            case PLAYERPOINTS:
                return setupPlayerPoints();

            default:
                return false;
        }
    }

    private boolean setupPlayerPoints() {
        var ppPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (ppPlugin != null) {
            playerPointsAPI = ((PlayerPoints) ppPlugin).getAPI();
            plugin.getLogger().info("Using PlayerPoints economy!");
            return true;
        }
        plugin.getLogger().severe("No economy plugin found!");
        return false;
    }

    public boolean hasMoney(OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                return vaultEconomy != null && vaultEconomy.has(player, amount);
            case PLAYERPOINTS:
                return playerPointsAPI != null && playerPointsAPI.look(player.getUniqueId()) >= amount;
            default:
                return false;
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                if (vaultEconomy == null) return false;
                return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            case PLAYERPOINTS:
                if (playerPointsAPI == null) return false;
                return playerPointsAPI.take(player.getUniqueId(), (int) amount);
            default:
                return false;
        }
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                if (vaultEconomy == null) return false;
                return vaultEconomy.depositPlayer(player, amount).transactionSuccess();
            case PLAYERPOINTS:
                if (playerPointsAPI == null) return false;
                playerPointsAPI.give(player.getUniqueId(), (int) amount);
                return true;
            default:
                return false;
        }
    }

    public EconomyType getEconomyType() {
        return economyType;
    }

    public String format(double amount) {
        switch (economyType) {
            case PLAYERPOINTS:
                return String.valueOf((int) amount);
            case VAULT:
            default:
                return vaultEconomy != null ? vaultEconomy.format(amount) : String.valueOf(amount);
        }
    }

    public boolean isVaultAvailable() {
        return vaultEconomy != null;
    }

    public void depositToTaxAccount(double amount) {
        if (economyType != EconomyType.VAULT || vaultEconomy == null) return;
        String accountName = plugin.getConfigManager().getString("tax.tax-account");
        OfflinePlayer taxPlayer = Bukkit.getOfflinePlayer(accountName);
        if (taxPlayer != null) {
            vaultEconomy.depositPlayer(taxPlayer, amount);
        }
    }
}

package com.hangrong.gui;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import com.hangrong.vendor.VendorItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GUIListener implements Listener {

    private final HangRong plugin;
    private final String BULK_TITLE_PREFIX = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', "&6Chọn số lượng"));

    public GUIListener(HangRong plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        Vendor vendor = plugin.getGuiManager().getOpenGuis().get(top);

        String viewTitle = event.getView().getTitle();
        String vendorTitlePrefix = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getString("gui.title")).split("%")[0];
        
        boolean isVendorGUI = (vendor != null) || (viewTitle.startsWith(vendorTitlePrefix));

        if (isVendorGUI) {
            event.setCancelled(true);
            if (vendor == null) {
                event.getWhoClicked().closeInventory();
                return;
            }
        } else {
            return;
        }

        if (viewTitle.startsWith(BULK_TITLE_PREFIX)) {
            handleBulkSelectClick(event, vendor);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int slot = event.getSlot();

        if (plugin.getConfigManager().getBoolean("gui.fill-empty-slots")) {
            if (slot >= vendor.getItemCount()) return;
        }

        VendorItem item = null;
        int count = 0;
        for (VendorItem vendorItem : vendor.getItems().values()) {
            if (count == slot) {
                item = vendorItem;
                break;
            }
            count++;
        }

        if (item == null) return;

        if (!item.hasStock()) {
            plugin.getMessages().send(player, "messages.out-of-stock", "item", item.getDisplayName());
            return;
        }

        plugin.getGuiManager().openBulkSelectGUI(player, vendor, item);
    }

    private void handleBulkSelectClick(InventoryClickEvent event, Vendor vendor) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int slot = event.getSlot();

        if (slot == 22) {
            plugin.getGuiManager().openVendorGUI(player, vendor);
            return;
        }

        int amount = switch (slot) {
            case 10 -> 1;
            case 12 -> 8;
            case 14 -> 16;
            case 16 -> 32;
            case 20 -> 64;
            default -> 0;
        };

        if (amount == 0) return;

        String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String itemDisplayName = clickedName.replaceFirst("^x\\d+ ", "");

        for (VendorItem item : vendor.getItems().values()) {
            if (item.getDisplayName().equalsIgnoreCase(itemDisplayName)) {
                processPurchase(player, vendor, item, amount);
                return;
            }
        }
    }

    private void processPurchase(Player buyer, Vendor vendor, VendorItem item, int amount) {
        if (!item.hasStock() || amount > item.getStock()) {
            plugin.getMessages().send(buyer, "messages.out-of-stock", "item", item.getDisplayName());
            return;
        }

        double totalPrice = plugin.getGuiManager().calculateBulkPrice(item, amount);

        if (!plugin.getEconomyManager().hasMoney(buyer, totalPrice)) {
            plugin.getMessages().send(buyer, "messages.not-enough-money");
            return;
        }

        if (!plugin.getEconomyManager().withdraw(buyer, totalPrice)) {
            plugin.getMessages().send(buyer, "messages.purchase-failed", "item", item.getDisplayName());
            return;
        }

        double sellerAmount = totalPrice;
        boolean taxApplied = false;

        if (plugin.getConfigManager().getBoolean("tax.enabled")) {
            if (!buyer.hasPermission(plugin.getConfigManager().getString("tax.bypass-permission"))) {
                double taxRate = vendor.getCustomTax() != null
                        ? vendor.getCustomTax()
                        : plugin.getConfigManager().getDouble("tax.percentage");
                double taxAmount = totalPrice * taxRate / 100.0;
                sellerAmount -= taxAmount;
                taxApplied = true;

                if (plugin.getEconomyManager().isVaultAvailable()) {
                    plugin.getEconomyManager().depositToTaxAccount(taxAmount);
                }
            }
        }

        String sellerUuid = vendor.getOwner();
        plugin.getEconomyManager().deposit(Bukkit.getOfflinePlayer(UUID.fromString(sellerUuid)), sellerAmount);

        item.removeStock(amount);
        if (item.getStock() <= 0) {
            vendor.removeItem(item.getId());
        }

        vendor.updateActivity();
        plugin.getVendorManager().getTransactionManager().addTransaction(
                vendor.getId(), buyer.getName(), item.getDisplayName(), amount, totalPrice, taxApplied);

        ItemStack giveItem = item.getItemStack();
        giveItem.setAmount(amount);

        for (ItemStack drop : buyer.getInventory().addItem(giveItem).values()) {
            buyer.getWorld().dropItemNaturally(buyer.getLocation(), drop);
        }

        plugin.getMessages().send(buyer, "messages.purchase-success",
                "item", item.getDisplayName(),
                "amount", String.valueOf(amount));

        if (taxApplied) {
            plugin.getMessages().send(buyer, "messages.tax-charged",
                    "tax", String.valueOf(vendor.getCustomTax() != null
                            ? vendor.getCustomTax()
                            : plugin.getConfigManager().getDouble("tax.percentage")));
        }

        plugin.getHologramManager().updateHologram(vendor);

        Player seller = Bukkit.getPlayer(UUID.fromString(sellerUuid));
        if (seller != null) {
            plugin.getMessages().send(seller, "messages.sale-notification",
                    "buyer", buyer.getName(),
                    "item", item.getDisplayName(),
                    "amount", String.valueOf(amount),
                    "price", plugin.getEconomyManager().format(sellerAmount));
        }

        plugin.getGuiManager().openVendorGUI(buyer, vendor);
    }

    @EventHandler
    public void onGUIClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        plugin.getGuiManager().getOpenGuis().remove(top);
    }
}

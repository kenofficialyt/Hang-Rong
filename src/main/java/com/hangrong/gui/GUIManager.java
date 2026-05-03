package com.hangrong.gui;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import com.hangrong.vendor.VendorItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIManager {

    private final HangRong plugin;
    private final Map<Inventory, Vendor> openGuis;

    public GUIManager(HangRong plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
    }

    public void openVendorGUI(Player player, Vendor vendor) {
        int rows = plugin.getConfigManager().getInt("gui.rows");
        String title = plugin.getConfigManager().getString("gui.title")
                .replace("%seller%", vendor.getId());
        title = ChatColor.translateAlternateColorCodes('&', title);

        Inventory gui = Bukkit.createInventory(null, rows * 9, title);
        openGuis.put(gui, vendor);

        int slot = 0;
        for (VendorItem item : vendor.getItems().values()) {
            if (slot >= rows * 9 - 1) break;
            gui.setItem(slot, createVendorItemIcon(item));
            slot++;
        }

        if (plugin.getConfigManager().getBoolean("gui.fill-empty-slots")) {
            ItemStack filler = createFillerItem();
            for (int i = slot; i < rows * 9; i++) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    public void openBulkSelectGUI(Player player, Vendor vendor, VendorItem item) {
        String title = ChatColor.translateAlternateColorCodes('&',
                "&6Chọn số lượng - " + item.getDisplayName());
        Inventory gui = Bukkit.createInventory(null, 3 * 9, title);

        int[] amounts = {1, 8, 16, 32, 64};
        int[] slots = {10, 12, 14, 16, 20};

        for (int i = 0; i < amounts.length; i++) {
            int amount = amounts[i];
            if (amount > item.getStock()) break;

            double totalPrice = calculateBulkPrice(item, amount);
            int discount = getDiscountForAmount(amount);

            ItemStack icon = new ItemStack(item.getItemStack().getType(), Math.min(amount, item.getStock()));
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "x" + amount + " " + item.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Giá: " + ChatColor.YELLOW + plugin.getEconomyManager().format(totalPrice));
                lore.add(ChatColor.GRAY + "Tồn kho: " + ChatColor.YELLOW + item.getStock());
                if (discount > 0) {
                    lore.add(ChatColor.YELLOW + "Giảm giá: " + ChatColor.RED + "-" + discount + "%");
                }
                lore.add("");
                lore.add(ChatColor.GREEN + "Click để mua!");
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            gui.setItem(slots[i], icon);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "← Quay lại");
            back.setItemMeta(backMeta);
        }
        gui.setItem(22, back);

        player.openInventory(gui);
    }

    private ItemStack createVendorItemIcon(VendorItem item) {
        ItemStack icon = item.getItemStack();
        icon.setAmount(1);

        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        String nameFormat = item.hasStock()
                ? plugin.getConfigManager().getString("gui.buy-item-name")
                : plugin.getConfigManager().getString("gui.out-of-stock-name");

        String name = nameFormat
                .replace("%s%", item.getDisplayName())
                .replace("%price%", plugin.getEconomyManager().format(item.getPrice()));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        if (item.hasStock()) {
            for (String line : plugin.getConfigManager().getStringList("gui.buy-item-lore")) {
                String processed = line
                        .replace("%price%", plugin.getEconomyManager().format(item.getPrice()))
                        .replace("%stock%", String.valueOf(item.getStock()))
                        .replace("%bulk%", "8")
                        .replace("%bulk_price%", plugin.getEconomyManager().format(calculateBulkPrice(item, 8)))
                        .replace("%discount%", String.valueOf(getDiscountForAmount(8)));
                lore.add(ChatColor.translateAlternateColorCodes('&', processed));
            }
        } else {
            for (String line : plugin.getConfigManager().getStringList("gui.out-of-stock-lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack createFillerItem() {
        Material material = Material.matchMaterial(plugin.getConfigManager().getString("gui.filler-item"));
        if (material == null) material = Material.GRAY_STAINED_GLASS_PANE;

        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getString("gui.filler-name")));
            filler.setItemMeta(meta);
        }
        return filler;
    }

    public double calculateBulkPrice(VendorItem item, int amount) {
        double basePrice = item.getPrice() * amount;
        int discount = getDiscountForAmount(amount);
        if (discount > 0) {
            basePrice *= (100.0 - discount) / 100.0;
        }
        return basePrice;
    }

    public int getDiscountForAmount(int amount) {
        if (!plugin.getConfigManager().getBoolean("bulk-pricing.enabled")) return 0;

        int bestDiscount = 0;
        var brackets = plugin.getConfigManager().getConfig().getConfigurationSection("bulk-pricing.discount-brackets");
        if (brackets == null) return 0;

        for (String key : brackets.getKeys(false)) {
            int bracketAmount = brackets.getInt(key + ".amount");
            int discount = brackets.getInt(key + ".discount");
            if (amount >= bracketAmount && discount > bestDiscount) {
                bestDiscount = discount;
            }
        }

        return bestDiscount;
    }

    public Map<Inventory, Vendor> getOpenGuis() {
        return openGuis;
    }
}

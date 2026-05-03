package com.hangrong.commands;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import com.hangrong.vendor.VendorItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class HangRongCommand implements CommandExecutor {

    private final HangRong plugin;

    public HangRongCommand(HangRong plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Chỉ người chơi mới dùng được lệnh này!");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "sell" -> handleSell(player, args);
            case "price" -> handlePrice(player, args);
            case "stock" -> handleStock(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            case "info" -> handleInfo(player, args);
            case "history" -> handleHistory(player, args);
            case "admin" -> handleAdmin(player, args);
            default -> showHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("hangrong.create")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong create <tên>");
            return;
        }

        String id = args[1];
        if (plugin.getVendorManager().getVendor(id) != null) {
            plugin.getMessages().send(player, "messages.vendor-exists", "vendor", id);
            return;
        }

        if (!plugin.getVendorManager().createVendor(player, id)) {
            plugin.getMessages().send(player, "messages.max-stalls-reached");
            return;
        }

        plugin.getMessages().send(player, "messages.vendor-created", "vendor", id);
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("hangrong.delete")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong delete <tên>");
            return;
        }

        String id = args[1];
        Vendor vendor = plugin.getVendorManager().getVendor(id);
        if (vendor == null) {
            plugin.getMessages().send(player, "messages.vendor-not-found", "vendor", id);
            return;
        }

        if (!vendor.getOwner().equals(player.getUniqueId().toString()) && !player.hasPermission("hangrong.admin.delete")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        plugin.getVendorManager().deleteVendor(id);
        plugin.getMessages().send(player, "messages.vendor-deleted", "vendor", id);
    }

    private void handleSell(Player player, String[] args) {
        if (!player.hasPermission("hangrong.sell")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            plugin.getMessages().send(player, "messages.no-item-in-hand");
            return;
        }

        List<Vendor> vendors = plugin.getVendorManager().getPlayerVendors(player);
        if (vendors.isEmpty()) {
            plugin.getMessages().sendRaw(player, "&cBạn chưa có sạp hàng nào! Dùng /hangrong create <tên>");
            return;
        }

        Vendor vendor = vendors.get(0);

        if (vendor.getItemCount() >= plugin.getConfigManager().getInt("vendor.max-items-per-stall")) {
            plugin.getMessages().send(player, "messages.max-items-reached");
            return;
        }

        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong sell <giá> [số-lượng]");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            plugin.getMessages().send(player, "messages.invalid-number");
            return;
        }

        int amount = item.getAmount();
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0 || amount > item.getAmount()) {
                    plugin.getMessages().send(player, "messages.invalid-amount");
                    return;
                }
            } catch (NumberFormatException e) {
                plugin.getMessages().send(player, "messages.invalid-number");
                return;
            }
        }

        String itemId = UUID.randomUUID().toString().substring(0, 8);
        VendorItem vendorItem = new VendorItem(itemId, item, price, amount);

        if (vendor.addItem(vendorItem)) {
            int newAmount = item.getAmount() - amount;
            if (newAmount <= 0) {
                player.getInventory().setItemInMainHand(null);
            } else {
                item.setAmount(newAmount);
            }

            plugin.getMessages().send(player, "messages.item-added",
                    "item", vendorItem.getDisplayName(),
                    "price", plugin.getEconomyManager().format(price));
            plugin.getHologramManager().updateHologram(vendor);
        }
    }

    private void handlePrice(Player player, String[] args) {
        if (!hasVendorPermission(player)) return;

        if (args.length < 3) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong price <tên-item> <giá>");
            return;
        }

        String itemName = args[1];
        double newPrice;
        try {
            newPrice = Double.parseDouble(args[2]);
            if (newPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            plugin.getMessages().send(player, "messages.invalid-number");
            return;
        }

        Vendor vendor = getFirstVendor(player);
        if (vendor == null) return;

        VendorItem item = findItem(vendor, itemName);
        if (item == null) {
            plugin.getMessages().sendRaw(player, "&cKhông tìm thấy mặt hàng &e" + itemName + "&c!");
            return;
        }

        item.setPrice(newPrice);
        plugin.getMessages().send(player, "messages.price-changed",
                "item", item.getDisplayName(),
                "price", plugin.getEconomyManager().format(newPrice));
        plugin.getHologramManager().updateHologram(vendor);
    }

    private void handleStock(Player player, String[] args) {
        if (!hasVendorPermission(player)) return;

        if (args.length < 3) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong stock <tên-item> <số-lượng>");
            return;
        }

        String itemName = args[1];
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            plugin.getMessages().send(player, "messages.invalid-number");
            return;
        }

        Vendor vendor = getFirstVendor(player);
        if (vendor == null) return;

        VendorItem item = findItem(vendor, itemName);
        if (item == null) {
            plugin.getMessages().sendRaw(player, "&cKhông tìm thấy mặt hàng &e" + itemName + "&c!");
            return;
        }

        item.addStock(amount);
        plugin.getMessages().send(player, "messages.stock-added",
                "item", item.getDisplayName(),
                "amount", String.valueOf(amount));
        plugin.getHologramManager().updateHologram(vendor);
    }

    private void handleRemove(Player player, String[] args) {
        if (!hasVendorPermission(player)) return;

        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong remove <tên-item>");
            return;
        }

        String itemName = args[1];
        Vendor vendor = getFirstVendor(player);
        if (vendor == null) return;

        VendorItem item = findItem(vendor, itemName);
        if (item == null) {
            plugin.getMessages().sendRaw(player, "&cKhông tìm thấy mặt hàng &e" + itemName + "&c!");
            return;
        }

        vendor.removeItem(item.getId());
        plugin.getMessages().send(player, "messages.item-removed", "item", item.getDisplayName());
        plugin.getHologramManager().updateHologram(vendor);
    }

    private void handleList(Player player) {
        plugin.getMessages().send(player, "messages.vendor-list-header");

        List<Vendor> vendors = plugin.getVendorManager().getPlayerVendors(player);
        if (vendors.isEmpty()) {
            plugin.getMessages().sendRaw(player, "&7Không có sạp hàng nào!");
        } else {
            for (Vendor vendor : vendors) {
                plugin.getMessages().sendRaw(player,
                        "&e" + vendor.getId() + " &7- Items: &f" + vendor.getItemCount()
                                + " &7- Location: &f" + formatLocation(vendor.getLocation()));
            }
        }

        plugin.getMessages().send(player, "messages.vendor-list-footer");
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong info <tên>");
            return;
        }

        Vendor vendor = plugin.getVendorManager().getVendor(args[1]);
        if (vendor == null) {
            plugin.getMessages().send(player, "messages.vendor-not-found", "vendor", args[1]);
            return;
        }

        if (!vendor.getOwner().equals(player.getUniqueId().toString()) && !player.hasPermission("hangrong.admin.others")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        plugin.getMessages().sendRaw(player, "&6&l=== Thông tin sạp hàng ===");
        plugin.getMessages().sendRaw(player, "&eTên: &f" + vendor.getId());
        Player ownerPlayer = Bukkit.getPlayer(UUID.fromString(vendor.getOwner()));
        String ownerName = ownerPlayer != null ? ownerPlayer.getName() : vendor.getOwner();
        plugin.getMessages().sendRaw(player, "&eOwner: &f" + ownerName);
        plugin.getMessages().sendRaw(player, "&eSố mặt hàng: &f" + vendor.getItemCount());
        plugin.getMessages().sendRaw(player, "&eLocation: &f" + formatLocation(vendor.getLocation()));

        if (vendor.getCustomTax() != null) {
            plugin.getMessages().sendRaw(player, "&eThuế tùy chỉnh: &f" + vendor.getCustomTax() + "%");
        }

        plugin.getMessages().sendRaw(player, "&6====================");
    }

    private void handleHistory(Player player, String[] args) {
        String vendorId = args.length >= 2 ? args[1] : null;

        if (vendorId == null) {
            List<Vendor> vendors = plugin.getVendorManager().getPlayerVendors(player);
            if (vendors.isEmpty()) {
                plugin.getMessages().sendRaw(player, "&cBạn không có sạp hàng nào!");
                return;
            }
            vendorId = vendors.get(0).getId();
        }

        Vendor vendor = plugin.getVendorManager().getVendor(vendorId);
        if (vendor == null) {
            plugin.getMessages().send(player, "messages.vendor-not-found", "vendor", vendorId);
            return;
        }

        if (!vendor.getOwner().equals(player.getUniqueId().toString()) && !player.hasPermission("hangrong.admin.others")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        plugin.getMessages().send(player, "messages.transaction-history-title", "vendor", vendorId);

        var transactions = plugin.getVendorManager().getTransactionManager().getTransactions(vendorId);
        if (transactions.isEmpty()) {
            plugin.getMessages().sendRaw(player, "&7Không có giao dịch nào!");
        } else {
            for (var t : transactions) {
                String taxStr = t.isTaxApplied() ? " &7(thuế)" : "";
                plugin.getMessages().sendRaw(player,
                        "&7[&e" + t.getFormattedTime() + "&7] &b" + t.getBuyerName()
                                + " &7đã mua &a" + t.getItemName() + " &7x&f" + t.getAmount()
                                + " &7- &e" + plugin.getEconomyManager().format(t.getPrice()) + taxStr);
            }
        }
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("hangrong.admin")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessages().sendRaw(player, "&cUsage: /hangrong admin <reload|settax|delete>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "reload" -> {
                plugin.getConfigManager().reload();
                plugin.getMessages().reload();
                plugin.getMessages().send(player, "messages.reload-success");
            }
            case "settax" -> {
                if (args.length < 4) {
                    plugin.getMessages().sendRaw(player, "&cUsage: /hangrong admin settax <sạp> <%>");
                    return;
                }
                Vendor vendor = plugin.getVendorManager().getVendor(args[2]);
                if (vendor == null) {
                    plugin.getMessages().send(player, "messages.vendor-not-found", "vendor", args[2]);
                    return;
                }
                try {
                    double tax = Double.parseDouble(args[3]);
                    if (tax < 0 || tax > 100) throw new NumberFormatException();
                    vendor.setCustomTax(tax);
                    plugin.getMessages().sendRaw(player, "&aĐã đặt thuế &e" + tax + "%&a cho sạp &e" + args[2]);
                } catch (NumberFormatException e) {
                    plugin.getMessages().send(player, "messages.invalid-number");
                }
            }
            case "delete" -> {
                if (args.length < 3) {
                    plugin.getMessages().sendRaw(player, "&cUsage: /hangrong admin delete <player>");
                    return;
                }
                var offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                var vendors = plugin.getVendorManager().getPlayerVendors(offlinePlayer.getPlayer() != null
                        ? offlinePlayer.getPlayer() : player);
                for (var v : vendors) {
                    plugin.getVendorManager().deleteVendor(v.getId());
                }
                plugin.getMessages().sendRaw(player, "&aĐã xóa tất cả sạp hàng của &e" + args[2]);
            }
            default -> plugin.getMessages().sendRaw(player, "&cLệnh không hợp lệ!");
        }
    }

    private void showHelp(Player player) {
        plugin.getMessages().send(player, "messages.help-header");
        plugin.getMessages().send(player, "messages.help-create");
        plugin.getMessages().send(player, "messages.help-delete");
        plugin.getMessages().send(player, "messages.help-sell");
        plugin.getMessages().send(player, "messages.help-price");
        plugin.getMessages().send(player, "messages.help-stock");
        plugin.getMessages().send(player, "messages.help-remove");
        plugin.getMessages().send(player, "messages.help-list");
        plugin.getMessages().send(player, "messages.help-info");
        plugin.getMessages().send(player, "messages.help-history");
        plugin.getMessages().send(player, "messages.help-admin");
        plugin.getMessages().send(player, "messages.help-footer");
    }

    private boolean hasVendorPermission(Player player) {
        if (!player.hasPermission("hangrong.sell")) {
            plugin.getMessages().send(player, "messages.no-permission");
            return false;
        }
        return true;
    }

    private Vendor getFirstVendor(Player player) {
        List<Vendor> vendors = plugin.getVendorManager().getPlayerVendors(player);
        if (vendors.isEmpty()) {
            plugin.getMessages().sendRaw(player, "&cBạn chưa có sạp hàng nào!");
            return null;
        }
        return vendors.get(0);
    }

    private VendorItem findItem(Vendor vendor, String itemName) {
        for (VendorItem item : vendor.getItems().values()) {
            if (item.getDisplayName().equalsIgnoreCase(itemName)
                    || item.getId().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    private String formatLocation(org.bukkit.Location loc) {
        return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }
}

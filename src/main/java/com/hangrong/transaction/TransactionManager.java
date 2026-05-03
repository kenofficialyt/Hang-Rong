package com.hangrong.transaction;

import com.hangrong.HangRong;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionManager {

    private final HangRong plugin;
    private final Map<String, List<Transaction>> transactions;

    public TransactionManager(HangRong plugin) {
        this.plugin = plugin;
        this.transactions = new HashMap<>();
        loadTransactions();
    }

    public void addTransaction(String vendorId, String buyerName, String itemName,
                               int amount, double price, boolean taxApplied) {
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                vendorId,
                buyerName,
                itemName,
                amount,
                price,
                taxApplied,
                System.currentTimeMillis()
        );

        transactions.computeIfAbsent(vendorId, k -> new ArrayList<>());
        transactions.get(vendorId).add(0, transaction);

        int maxEntries = plugin.getConfigManager().getInt("transaction-history.max-entries-per-vendor");
        if (transactions.get(vendorId).size() > maxEntries) {
            transactions.get(vendorId).subList(maxEntries, transactions.get(vendorId).size()).clear();
        }
    }

    public List<Transaction> getTransactions(String vendorId) {
        return transactions.getOrDefault(vendorId, Collections.emptyList());
    }

    public void clearTransactions(String vendorId) {
        transactions.remove(vendorId);
    }

    public void loadTransactions() {
        File file = new File(plugin.getDataFolder(), "transactions.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String vendorId : config.getKeys(false)) {
            List<Transaction> vendorTransactions = new ArrayList<>();
            var section = config.getConfigurationSection(vendorId);
            if (section == null) continue;

            for (String key : section.getKeys(false)) {
                try {
                    Transaction t = new Transaction(
                            key,
                            vendorId,
                            section.getString(key + ".buyer"),
                            section.getString(key + ".item"),
                            section.getInt(key + ".amount"),
                            section.getDouble(key + ".price"),
                            section.getBoolean(key + ".taxApplied"),
                            section.getLong(key + ".timestamp")
                    );
                    vendorTransactions.add(t);
                } catch (Exception e) {
                    plugin.getLogger().warning("Loi load giao dich " + key + ": " + e.getMessage());
                }
            }

            vendorTransactions.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            transactions.put(vendorId, vendorTransactions);
        }

        plugin.getLogger().info("Da load giao dich cho " + transactions.size() + " sạp hàng!");
    }

    public void saveTransactions() {
        File file = new File(plugin.getDataFolder(), "transactions.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, List<Transaction>> entry : transactions.entrySet()) {
            String vendorId = entry.getKey();
            for (Transaction t : entry.getValue()) {
                String path = vendorId + "." + t.getId();
                config.set(path + ".buyer", t.getBuyerName());
                config.set(path + ".item", t.getItemName());
                config.set(path + ".amount", t.getAmount());
                config.set(path + ".price", t.getPrice());
                config.set(path + ".taxApplied", t.isTaxApplied());
                config.set(path + ".timestamp", t.getTimestamp());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu giao dịch: " + e.getMessage());
        }

        cleanOldTransactions();
    }

    public void cleanOldTransactions() {
        int days = plugin.getConfigManager().getInt("transaction-history.auto-delete-days");
        if (days <= 0) return;

        long cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        for (List<Transaction> vendorTransactions : transactions.values()) {
            vendorTransactions.removeIf(t -> t.getTimestamp() < cutoff);
        }
    }

    public static class Transaction {
        private final String id;
        private final String vendorId;
        private final String buyerName;
        private final String itemName;
        private final int amount;
        private final double price;
        private final boolean taxApplied;
        private final long timestamp;

        public Transaction(String id, String vendorId, String buyerName, String itemName,
                           int amount, double price, boolean taxApplied, long timestamp) {
            this.id = id;
            this.vendorId = vendorId;
            this.buyerName = buyerName;
            this.itemName = itemName;
            this.amount = amount;
            this.price = price;
            this.taxApplied = taxApplied;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public String getVendorId() { return vendorId; }
        public String getBuyerName() { return buyerName; }
        public String getItemName() { return itemName; }
        public int getAmount() { return amount; }
        public double getPrice() { return price; }
        public boolean isTaxApplied() { return taxApplied; }
        public long getTimestamp() { return timestamp; }

        public String getFormattedTime() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timestamp));
        }
    }
}

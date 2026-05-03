package com.hangrong.utils;

import com.hangrong.HangRong;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {

    private final HangRong plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public Messages(HangRong plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String get(String path) {
        String message = messagesConfig.getString(path, path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String get(String path, String... placeholders) {
        String message = get(path);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        return message;
    }

    public String getPrefix() {
        return get("messages.prefix");
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(getPrefix() + get(path));
    }

    public void send(CommandSender sender, String path, String... placeholders) {
        sender.sendMessage(getPrefix() + get(path, placeholders));
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(getPrefix() + ChatColor.translateAlternateColorCodes('&', message));
    }
}

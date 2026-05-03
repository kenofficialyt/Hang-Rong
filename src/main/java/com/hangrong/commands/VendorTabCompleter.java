package com.hangrong.commands;

import com.hangrong.HangRong;
import com.hangrong.vendor.Vendor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VendorTabCompleter implements TabCompleter {

    private final HangRong plugin;

    public VendorTabCompleter(HangRong plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return null;

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] mainCommands = {"create", "delete", "sell", "price", "stock", "remove", "list", "info", "history"};
            if (player.hasPermission("hangrong.admin")) {
                completions.add("admin");
            }
            for (String cmd : mainCommands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create" -> completions.add("<tên>");
                case "delete", "info", "history" -> completions.addAll(getPlayerVendorNames(player));
                case "admin" -> {
                    if (player.hasPermission("hangrong.admin")) {
                        completions.add("reload");
                        completions.add("settax");
                        completions.add("delete");
                    }
                }
            }
            return completions.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return null;
    }

    private List<String> getPlayerVendorNames(Player player) {
        return plugin.getVendorManager().getPlayerVendors(player).stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());
    }
}

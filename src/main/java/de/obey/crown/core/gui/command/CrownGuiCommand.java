package de.obey.crown.core.gui.command;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:37
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.CrownPluginRegistry;
import de.obey.crown.core.data.plugin.Messanger;
import de.obey.crown.core.gui.CrownGuiService;
import de.obey.crown.core.gui.GuiLoader;
import de.obey.crown.core.gui.GuiRegistry;
import de.obey.crown.core.gui.model.CrownGui;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CrownGuiCommand implements CommandExecutor, TabCompleter {

    private final Messanger messanger;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!messanger.hasPermission(sender, "core.command.gui"))
            return false;

        if(args.length == 0) {
            messanger.sendCommandSyntax(sender, "/crowngui", "/crowngui list", "/crowngui open <gui-id>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "open" -> handleOpen(sender, args);
            default ->  messanger.sendCommandSyntax(sender, "/crowngui", "/crowngui list", "/crowngui open <gui-id>");
        }

        return false;
    }

    private void handleList(final CommandSender sender) {
        messanger.sendNonConfigMessage(sender, "%prefix% Loaded GUIs %accent%(" + GuiRegistry.all().size() + "): ");

        GuiRegistry.all().values().forEach(gui -> {
            messanger.sendNonConfigMessage(sender, " &7- %white% " + gui.getKey());
        });
    }

    private void handleOpen(final CommandSender sender, String[] args) {
        if (args.length < 2) {
            messanger.sendCommandSyntax(sender, "/crowngui", "/crowngui list", "/crowngui open <gui-id>");
            return;
        }

        final String key = args[1];
        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            messanger.sendNonConfigMessage(sender, "%prefix% unknown gui: " + key);
            return;
        }

        Player target;

        if (args.length >= 3) {
            if(!messanger.isOnline(sender, args[2])) {
                return;
            }

            target = Bukkit.getPlayer(args[2]);
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole must specify a player.");
                messanger.sendNonConfigMessage(sender, "%prefix% Console must specify a target player.");
                return;
            }
            target = player;
        }

        CrownGuiService.open(target, key);
        messanger.sendNonConfigMessage(sender, "%prefix% Opened %accent%" + key + " %white%for %accent%" + target.getName() + "%white%.");
    }

    private void handleReload(final CommandSender sender) {
        messanger.sendNonConfigMessage(sender, "%prefix% Reloading all crown GUIs ...");
        final long start = System.currentTimeMillis();

        for (final Plugin plugin : CrownPluginRegistry.getCrownPlugins().values()) {
            GuiLoader.loadAll(plugin);
        }

        messanger.sendNonConfigMessage(sender, "%prefix%&a Reloaded %white%all crown GUIs in %accent%" + (System.currentTimeMillis() - start) + "ms%white%.");
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        final ArrayList<String> list = new ArrayList<>();

        if (!(sender instanceof Player))
            return list;

        if (!sender.hasPermission("core.command.gui"))
            return list;

        if (args.length == 1) {
            list.add("list");
            list.add("open");
            list.add("reload");
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("open")) {
                list.addAll(GuiRegistry.all().keySet());
            }
        }

        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("open")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    list.add(onlinePlayer.getName());
                }
            }
        }

        final String argument = args[args.length - 1];
        if (!argument.isEmpty())
            list.removeIf(value -> !value.toLowerCase().startsWith(argument.toLowerCase()));

        Collections.sort(list);

        return list;
    }
}

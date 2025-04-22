/* CrownPlugins - CrownCore */
/* 06.10.2024 - 00:12 */

package de.obey.crown.core.command;

import de.obey.crown.core.CrownCore;
import de.obey.crown.core.PluginConfig;
import de.obey.crown.core.data.plugin.Messanger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@NonNull
public final class CoreCommand implements CommandExecutor, TabCompleter {

    private final Messanger messanger;
    private final PluginConfig pluginConfig;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            messanger.sendNonConfigMessage(sender, "%prefix% Running %accent%CrownCore %white%version %accent%&o" + CrownCore.getInstance().getDescription().getVersion() + "%white% by %accent%@Obeeyyyy%white%.");
            return false;
        }

        if (!messanger.hasPermission(sender, "command.core.reload"))
            return false;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                pluginConfig.loadMessages();
                pluginConfig.loadConfig();
                pluginConfig.loadSounds();
                messanger.sendMessage(sender, "plugin-reloaded", new String[]{"plugin"}, CrownCore.getInstance().getName());
                return false;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (args[1].equalsIgnoreCase("messages")) {
                    pluginConfig.loadMessages();
                    messanger.sendMessage(sender, "messages-reloaded", new String[]{"plugin"}, CrownCore.getInstance().getName());

                    return false;
                }

                if (args[1].equalsIgnoreCase("pluginConfig")) {
                    pluginConfig.loadConfig();
                    messanger.sendMessage(sender, "pluginConfig-reloaded", new String[]{"plugin"}, CrownCore.getInstance().getName());

                    return false;
                }
            }
        }

        messanger.sendCommandSyntax(sender, "/core",
                "/core reload : Reloads pluginConfig and messages.",
                "/core reload <pluginConfig/messages> : Reloads pluginConfig or messages.");

        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, Command command, String s, String[] args) {

        final ArrayList<String> list = new ArrayList<>();

        if (!sender.hasPermission("command.core.reload"))
            return list;

        if (args.length == 1) {
            list.add("reload");
        }

        if (args.length == 2) {
            list.add("pluginConfig");
            list.add("messages");
        }


        final String argument = args[args.length - 1];
        if (!argument.isEmpty())
            list.removeIf(value -> !value.toLowerCase().startsWith(argument.toLowerCase()));

        Collections.sort(list);

        return list;
    }

}

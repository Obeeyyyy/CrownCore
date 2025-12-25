/* CrownPlugins - CrownCore */
/* 21.08.2024 - 01:55 */

package de.obey.crown.core.command;

import de.obey.crown.core.data.plugin.Messanger;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.util.Teleporter;
import de.obey.crown.core.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public final class LocationCommand implements CommandExecutor, TabCompleter {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    private final Messanger messanger;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player))
            return false;

        if (!messanger.hasPermission(sender, "core.command.location"))
            return false;

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("list")) {

                player.sendMessage(TextUtil.translateColors("%prefix% Current Locations &8(%accent%" + LocationHandler.getLocations().size() + "&8)"));

                LocationHandler.getLocations().keySet().forEach(name -> {
                    final Location location = LocationHandler.getLocation(name);
                    player.sendMessage(TextUtil.translateColors("§8    => %accent%" + name + "§8 - (%white% " + location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " §8)"));
                });

                return false;
            }

        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {

                LocationHandler.setLocation(args[1], player.getLocation());
                messanger.sendMessage(sender, "location-set", new String[]{"name"}, args[1]);

                return false;
            }

            if (args[0].equalsIgnoreCase("delete")) {

                LocationHandler.deleteLocation(args[1]);
                messanger.sendMessage(sender, "location-deleted", new String[]{"name"}, args[1]);

                return false;
            }

            if (args[0].equalsIgnoreCase("tp")) {
                Teleporter.teleportInstant(player, args[1]);
                return false;
            }
        }

        if(args.length == 3) {
            final String locationName = args[1];

            if(!LocationHandler.getLocations().containsKey(locationName)) {
                messanger.sendMessage(sender, "location-invalid", new String[]{"name"}, locationName);
                return false;
            }

            try {

                final float value = Float.parseFloat(args[2]);

                final Location location = LocationHandler.getLocation(locationName);

                if (args[0].equalsIgnoreCase("setyaw")) {

                    location.setYaw(value);
                    LocationHandler.getLocations().put(locationName, location);
                    LocationHandler.saveLocations();

                    messanger.sendNonConfigMessage(sender, "%prefix% You have set the yaw for location '" + locationName + "' to " + value + ".");

                    return false;
                }

                if (args[0].equalsIgnoreCase("setpitch")) {
                    location.setPitch(value);
                    LocationHandler.getLocations().put(locationName, location);
                    LocationHandler.saveLocations();

                    messanger.sendNonConfigMessage(sender, "%prefix% You have set the pitch for location '" + locationName + "' to " + value + ".");

                    return false;
                }

                if (args[0].equalsIgnoreCase("setx")) {
                    location.setX(value);
                    LocationHandler.getLocations().put(locationName, location);
                    LocationHandler.saveLocations();

                    messanger.sendNonConfigMessage(sender, "%prefix% You have set the x value for location '" + locationName + "' to " + value + ".");

                    return false;
                }

                if (args[0].equalsIgnoreCase("setz")) {
                    location.setZ(value);
                    LocationHandler.getLocations().put(locationName, location);
                    LocationHandler.saveLocations();

                    messanger.sendNonConfigMessage(sender, "%prefix% You have set the z value for location '" + locationName + "' to " + value + ".");

                    return false;
                }

                if (args[0].equalsIgnoreCase("sety")) {
                    location.setY(value);
                    LocationHandler.getLocations().put(locationName, location);
                    LocationHandler.saveLocations();

                    messanger.sendNonConfigMessage(sender, "%prefix% You have set the y value for location '" + locationName + "' to " + value + ".");
                    return false;
                }

            }catch (final NumberFormatException exception) {
                messanger.sendNonConfigMessage(player, "%prefix% Invalid number.");
            }
        }

        messanger.sendCommandSyntax(sender, "/location",
                "/location list",
                "/location set <name>",
                "/location delete <name>",
                "/location tp <name>",
                "/location setyaw <name> <value>",
                "/location setpitch <name> <value>",
                "/location setx <name> <value>",
                "/location setz <name> <value>",
                "/location sety <name> <value>")
        ;

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {

        final ArrayList<String> list = new ArrayList<>();

        if (!(sender instanceof Player))
            return list;

        if (!sender.hasPermission("core.command.location"))
            return list;

        if (args.length == 1) {
            list.add("list");
            list.add("set");
            list.add("delete");
            list.add("tp");
            list.add("setyaw");
            list.add("setpitch");
            list.add("setx");
            list.add("serz");
            list.add("sety");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp")
                    || args[0].equalsIgnoreCase("setyaw")
                    || args[0].equalsIgnoreCase("setpitch")
                    || args[0].equalsIgnoreCase("setx")
                    || args[0].equalsIgnoreCase("setz")
                    || args[0].equalsIgnoreCase("sety")
            ) {
                list.addAll(LocationHandler.getLocations().keySet());
            } else {
                if (args[0].equalsIgnoreCase("set")) {
                    list.add("name");
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

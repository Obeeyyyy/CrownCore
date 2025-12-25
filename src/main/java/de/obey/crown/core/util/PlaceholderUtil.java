package de.obey.crown.core.util;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:03
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.Placeholders;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderUtil {

    private static final Map<String, Function<OfflinePlayer, String>> PLACEHOLDERS = Maps.newConcurrentMap();

    private static boolean papiEnabled = false;

    public static void init() {
        papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if(papiEnabled)
            new Placeholders().register();

        register("player", OfflinePlayer::getName);
        register("uuid", p -> p.getUniqueId().toString());
    }

    public static void register(final String key, final Function<OfflinePlayer, String> resolver) {
        PLACEHOLDERS.put(key.toLowerCase(), resolver);
    }

    public static String resolve(final OfflinePlayer player, final String input) {
        if (input == null || input.isEmpty()) return input;

        String result = input;

        for (Map.Entry<String, Function<OfflinePlayer, String>> entry : PLACEHOLDERS.entrySet()) {
            String token = "%" + entry.getKey() + "%";
            if (result.contains(token)) {
                result = result.replace(
                        token,
                        safe(entry.getValue(), player)
                );
            }
        }

        if (papiEnabled) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (final Throwable ignored) {}
        }

        return result;
    }

    public static List<String> resolve(final OfflinePlayer player, final List<String> input) {
        if (input == null || input.isEmpty()) return input;

        final List<String> resolved = new ArrayList<>(input.size());
        for (final String line : input) {
            resolved.add(resolve(player, line));
        }

        return resolved;
    }


    private static String safe(final Function<OfflinePlayer, String> resolver, final OfflinePlayer player) {
        try {
            final String value = resolver.apply(player);
            return value != null ? value : "";
        } catch (Exception ex) {
            return "";
        }
    }


}

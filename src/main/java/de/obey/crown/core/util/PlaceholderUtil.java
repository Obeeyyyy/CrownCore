package de.obey.crown.core.util;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:03
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.data.plugin.placeholders.Placeholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderUtil {

    private static final Map<String, Function<OfflinePlayer, String>> PLACEHOLDERS = Maps.newConcurrentMap();

    public static boolean papiEnabled = false;
    public static Placeholders placeholders;

    public static boolean isPapiEnabled() {
        if (!papiEnabled) {
            try {
                papiEnabled = Bukkit.getPluginManager() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
            } catch (final Throwable ignored) {}
        }
        return papiEnabled;
    }

    public static void initialize() {
        papiEnabled = Bukkit.getPluginManager() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        placeholders = new Placeholders(CrownCore.getInstance().getPluginConfig());

        if (papiEnabled)
            placeholders.register();

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

        if (isPapiEnabled()) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (final Throwable ignored) {}
        }

        return result;
    }

    public static List<String> resolve(final OfflinePlayer player, final List<String> input) {
        if (input == null || input.isEmpty()) return input;

        List<String> result = new ArrayList<>(input.size());
        for (final String line : input) {
            if (line == null) continue;
            String processed = line;
            for (Map.Entry<String, Function<OfflinePlayer, String>> entry : PLACEHOLDERS.entrySet()) {
                String token = "%" + entry.getKey() + "%";
                if (processed.contains(token)) {
                    processed = processed.replace(token, safe(entry.getValue(), player));
                }
            }
            result.add(processed);
        }

        if (isPapiEnabled()) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (final Throwable ignored) {}
        }

        final List<String> flattened = new ArrayList<>(result.size());
        for (final String line : result) {
            if (line == null) continue;
            if (line.contains("\n") || line.contains("\r")) {
                for (final String sub : line.split("\r?\n")) {
                    flattened.add(sub);
                }
            } else {
                flattened.add(line);
            }
        }

        return flattened;
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

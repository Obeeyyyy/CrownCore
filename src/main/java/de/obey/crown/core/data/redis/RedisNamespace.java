package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 17:02
    Project: CrownCore
*/

import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class RedisNamespace {

    public static String namespaced(final Plugin plugin, final String key) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(key, "key");

        final String pluginName = plugin.getName().toLowerCase();

        if (key.contains(":")) {
            throw new IllegalArgumentException(
                    "message type must NOT contain namespace already: " + key
            );
        }

        return pluginName + ":" + key.toLowerCase();
    }

    public static String extractPlugin(final String namespaced) {
        int i = namespaced.indexOf(":");
        if (i <= 0) throw new IllegalArgumentException("Invalid namespaced key: " + namespaced);
        return namespaced.substring(0, i);
    }

    public static String extractKey(final String namespaced) {
        int i = namespaced.indexOf(":");
        if (i <= 0) throw new IllegalArgumentException("Invalid namespaced key: " + namespaced);
        return namespaced.substring(i + 1);
    }
}


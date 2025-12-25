package de.obey.crown.core.data.plugin;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 13:46
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class CrownPluginRegistry {

    @Getter
    private static final Map<String, Plugin> crownPlugins = Maps.newConcurrentMap();

    public static void register(final Plugin plugin) {
        crownPlugins.put(plugin.getName(), plugin);
    }

}

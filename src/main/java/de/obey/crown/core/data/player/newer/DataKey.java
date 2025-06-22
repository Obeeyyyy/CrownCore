package de.obey.crown.core.data.player.newer;

import org.bukkit.plugin.Plugin;

public record DataKey<T>(Plugin plugin, String key, Class<T> type, Object defaultValue) {

    /***
     * Returns the data key's path containing of
     * 'plugin'.'path'
     *
     * @return String
     */
    public String path() {
        return plugin.getName().toLowerCase() + "." + key;
    }
}

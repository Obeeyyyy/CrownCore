package de.obey.crown.core.data.plugin.storage.datakey;

import org.bukkit.plugin.Plugin;

public class PluginDataKey<T> extends DataKey<T> {

    public PluginDataKey(String name, Plugin plugin, Class<T> dataType, T defaultValue, String sqlDataType) {
        super(name, plugin, dataType, defaultValue, sqlDataType);
    }

    public PluginDataKey(String name, Plugin plugin, Class<T> dataType, String sqlDataType) {
        super(name, plugin, dataType, sqlDataType);
    }
}

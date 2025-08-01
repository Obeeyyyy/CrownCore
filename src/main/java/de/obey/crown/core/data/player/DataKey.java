package de.obey.crown.core.data.player;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

@Getter
public class DataKey<T> {

    private final String name, plugin;
    private final Class<T> dataType;
    private final Object defaultValue;
    private final String sqlDataType;

    public DataKey(final String name, final Plugin plugin, final Class<T> dataType, final Object defaultValue, final String sqlDataType) {
        this.name = name;
        this.plugin = plugin.getName().toLowerCase();
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.sqlDataType = sqlDataType;
    }
}

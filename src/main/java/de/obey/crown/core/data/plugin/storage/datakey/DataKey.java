package de.obey.crown.core.data.plugin.storage.datakey;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

@Getter @Setter
public class DataKey<T> {

    private final String name, plugin;
    private final Class<T> dataType;
    private final Object defaultValue;
    private final String sqlDataType;

    private boolean primaryKey = false;
    private boolean autoIncrement = false;
    private boolean notNull = false;
    private boolean unique = false;

    public DataKey(final String name, final Plugin plugin, final Class<T> dataType, final Object defaultValue, final String sqlDataType) {
        this.name = name;
        this.plugin = plugin.getName().toLowerCase();
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.sqlDataType = sqlDataType;
    }
}

package de.obey.crown.core.data.plugin.storage;

import de.obey.crown.core.data.player.DataKey;
import de.obey.crown.core.data.player.DataKeyRegistry;
import lombok.Getter;

import java.util.List;

@Getter
public class PluginDataSchema {

    private final String pluginName;
    private final List<DataKey<?>> dataKeys;

    public PluginDataSchema(final String pluginName) {
        this.pluginName = pluginName;
        this.dataKeys = DataKeyRegistry.getPluginKeys(pluginName);
    }
}

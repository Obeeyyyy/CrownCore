package de.obey.crown.core.data.plugin.storage.player;

import de.obey.crown.core.data.plugin.storage.datakey.DataKey;
import de.obey.crown.core.data.plugin.storage.datakey.DataKeyRegistry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlayerDataSchema {

    private final String pluginName;
    private final List<DataKey<?>> dataKeys = new ArrayList<>();

    public PlayerDataSchema(final String pluginName) {
        this.pluginName = pluginName.toLowerCase();
        dataKeys.addAll(DataKeyRegistry.getPluginKeys(this.pluginName));
    }
}

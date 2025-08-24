package de.obey.crown.core.data.plugin.storage.plugin;

import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.data.plugin.storage.datakey.DataKey;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class PluginDataSchema {

    private final String pluginName;
    private final String tableName;
    private final String primaryKeyName;
    private final List<DataKey<?>> dataKeys = new ArrayList<>();
    private final CrownConfig crownConfig;

    public PluginDataSchema(final CrownConfig crownConfig, final String tableName, final String primaryKeyName) {
        this.crownConfig = crownConfig;
        this.pluginName = crownConfig.getPlugin().getName().toLowerCase();
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;
    }

    public void register() {
        CrownCore.getInstance().getPluginStorageManager().registerPluginDataPlugin(crownConfig, this);
    }

    public void add(final DataKey<?> dataKey) {
        dataKeys.add(dataKey);
    }

    public void add(final DataKey<?>... list) {
        dataKeys.addAll(Arrays.asList(list));
    }
}

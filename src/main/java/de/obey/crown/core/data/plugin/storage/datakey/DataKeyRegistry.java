package de.obey.crown.core.data.plugin.storage.datakey;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

@UtilityClass
public class DataKeyRegistry {

    /***
     * Map containing all DataKeys
     * key- plugin name
     * value - map by (keyname, key)
     */
    @Getter
    private final Map<String, Map<String, DataKey<?>>> registry = Maps.newConcurrentMap();

    public void register(final DataKey<?> key) {
        if(registry.containsKey(key.getPlugin())) {
            registry.get(key.getPlugin()).put(key.getName(), key);
            return;
        }

        final Map<String, DataKey<?>> temp = Maps.newConcurrentMap();
        temp.put(key.getName(), key);

        registry.put(key.getPlugin(), temp);
    }

    public void register(final DataKey<?>... keys) {
        for (final DataKey<?> key : keys) {
            register(key);
        }
    }

    public DataKey<?> get(String plugin, final String keyName) {
        plugin = plugin.toLowerCase();

        if(!registry.containsKey(plugin))
            return null;

        return registry.get(plugin).get(keyName);
    }

    public Collection<DataKey<?>> getPluginKeys(final String plugin) {
        return registry.get(plugin).values();
    }

    public boolean pluginHasKeys(final String plugin) {
        return registry.containsKey(plugin);
    }
}


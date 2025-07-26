package de.obey.crown.core.data.player;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;
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

    public DataKey<?> get(final String plugin, final String keyName) {
        if(!registry.containsKey(plugin))
            return null;

        return registry.get(plugin).get(keyName);
    }

    public List<DataKey<?>> getPluginKeys(final String plugin) {
        return (List<DataKey<?>>) registry.get(plugin).values();
    }

    public boolean pluginHasKeysw(final String plugin) {
        return registry.containsKey(plugin);
    }
}


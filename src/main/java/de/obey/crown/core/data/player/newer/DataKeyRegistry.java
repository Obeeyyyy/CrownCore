package de.obey.crown.core.data.player.newer;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

@UtilityClass
public class DataKeyRegistry {
    private final Map<String, DataKey<?>> registry = Maps.newConcurrentMap();

    public void register(final DataKey<?> key) {
        registry.put(key.path(), key);
    }

    public DataKey<?> get(final String plugin, final String path) {
        return registry.get(plugin + "." + path);
    }

    public Collection<DataKey<?>> getRegistryValues() {
        return registry.values();
    }
}


package de.obey.crown.core.data.plugin.storage.datakey;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

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

        CrownCore.log.debug("registering datakey");
        CrownCore.log.debug(" - name: " + key.getName());
        CrownCore.log.debug(" - plugin: " + key.getPlugin());

        registry.computeIfAbsent(key.getPlugin(), k -> Maps.newConcurrentMap()).put(key.getName(), key);
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

    public boolean pluginHasKeys(final String pluginName) {
        return registry.containsKey(pluginName) || registry.containsKey(pluginName.toLowerCase());
    }

    public boolean pluginHasKeys(final Plugin plugin) {
        return pluginHasKeys(plugin.getName());
    }
}


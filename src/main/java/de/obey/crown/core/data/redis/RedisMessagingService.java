package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 17:06
    Project: CrownCore
*/

import de.obey.crown.core.noobf.CrownCore;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

@RequiredArgsConstructor
public final class RedisMessagingService {

    private final Plugin plugin;

    public <T extends RedisMessage> void registerType(final String key, final Class<T> clazz) {
        // This might not be strictly necessary with the new flow if listen() does registration, 
        // but keeping it for pre-registration if needed.
        // For now, we'll ensure the registry exists.
        String namespace = RedisNamespace.namespaced(plugin, key); // returns e.g. "plugin:key"
        // But our bus expects namespace and type separate.
        // Let's assume 'plugin.getName()' is the namespace and 'key' is the type.
        
        CrownCore.getInstance().getRedisMessageBus().registry(plugin.getName()).register(key, clazz, (msg) -> {});
    }

    public <T extends RedisMessage> void publish(final T msg) {
        String type = msg.type(); 
        // We assume the message type string is just the key, and namespace is the plugin name.
        // Or if 'msg.type()' returns "plugin:key", we split it.
        // The previous implementation of RedisNamespace suggested "pluginname:key".
        
        // Let's use RedisNamespace logic to ensure consistency if possible, 
        // but here we simply need to broadcast.
        
        // If msg.type() is "myplugin:user_update", we need to split or use it as is?
        // The Bus expects (namespace, type, payload).
        
        // Let's assume convention: Namespace = Current Plugin Name, Type = msg.type().
        // Use the plugin passed in constructor as the owner.
        CrownCore.getInstance().getRedisMessageBus().publish(plugin.getName(), msg.type(), msg);
    }

    public <T extends RedisMessage> void listen(final String key, final Class<T> type, final Consumer<T> consumer) {
        CrownCore.getInstance().getRedisMessageBus().registry(plugin.getName()).register(key, type, consumer);
    }
}


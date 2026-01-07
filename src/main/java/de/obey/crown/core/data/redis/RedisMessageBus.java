package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 16:58
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RedisMessageBus {

    private final RedisManager redisManager;
    private final IMessageSerializer serializer;

    @Getter
    private final Map<String, RedisMessageRegistry> registries = Maps.newConcurrentMap();

    public RedisMessageBus(final RedisManager redisManager, final IMessageSerializer serializer) {
        this.redisManager = redisManager;
        this.serializer = serializer;
    }

    public RedisMessageRegistry registry(final String namespace) {
        return registries.computeIfAbsent(namespace, v -> new RedisMessageRegistry());
    }

    public RedisMessageRegistry registry(final Plugin plugin) {
        return registry(plugin.getName().toLowerCase());
    }

    public <T> void publish(final String namespace, final String type, final T message) {
        if (redisManager.getPubConnection() == null) return;

        final RedisMessageEnvelope env = new RedisMessageEnvelope();

        env.namespace = namespace;
        env.type = type;
        env.payload = serializer.serialize(message);

        redisManager.getPubConnection().async().publish("crown:bus", serializer.serialize(env));
    }

    public <T> void publish(final Plugin plugin, final String type, final T message) {
        publish(plugin.getName().toLowerCase(), type, message);
    }

    public void init() {
        redisManager.getSubConnection().addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                if (!channel.equals("crown:bus")) return;

                try {
                    final RedisMessageEnvelope env = serializer.deserialize(message, RedisMessageEnvelope.class);
                    if (env == null || env.namespace == null || env.type == null) return;

                    final RedisMessageRegistry registry = registries.get(env.namespace);
                    if (registry == null) return;

                    final List<Consumer<Object>> handlers = registry.getHandlers(env.type);
                    if (handlers == null || handlers.isEmpty()) return;

                    final Class<?> typeClass = registry.getType(env.type);
                    if (typeClass == null) return;

                    final Object obj = serializer.deserialize(env.payload, typeClass);

                    handlers.forEach(handler -> {
                        try {
                            handler.accept(obj);
                        } catch (final Exception exception) {
                            exception.printStackTrace();
                        }
                    });
                } catch (final Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        redisManager.getSubConnection().sync().subscribe("crown:bus");
    }
}
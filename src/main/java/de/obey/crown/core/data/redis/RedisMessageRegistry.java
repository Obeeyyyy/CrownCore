package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 16:56
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NoArgsConstructor
public class RedisMessageRegistry {

    private final Map<String, List<Consumer<Object>>> handlers = Maps.newConcurrentMap();
    private final Map<String, Class<?>> types = Maps.newConcurrentMap();

    public <T> void register(final String messageType, final Class<T> type, final Consumer<T> handler) {
        this.types.putIfAbsent(messageType, type);
        this.handlers.computeIfAbsent(messageType, k -> new ArrayList<>()).add((Consumer<Object>) handler);
    }

    public List<Consumer<Object>> getHandlers(final String messageType) {
        return handlers.get(messageType);
    }

    public Class<?> getType(final String messageType) {
        return types.get(messageType);
    }
}

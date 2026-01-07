package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 16:26
    Project: CrownCore
*/

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IRedisService {

    CompletableFuture<String> get(final String key);
    CompletableFuture<Void> set(final String key, final String value);
    CompletableFuture<Void> set(final String key, final String value, final Duration ttl);
    CompletableFuture<Boolean> exists(final String key);
    CompletableFuture<Boolean> delete(final String key);
    CompletableFuture<Long> publish(final String channel, final String message);

    void subscribe(final String channel, final Consumer<String> handler);
    void unsubscribe(final String channel);
}

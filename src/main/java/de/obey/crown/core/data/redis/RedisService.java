package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 16:27
    Project: CrownCore
*/

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class RedisService implements IRedisService {

    private final RedisManager manager;
    private final ExecutorService executor;

    @Override
    public CompletableFuture<String> get(final String key) {
        return CompletableFuture.supplyAsync(() ->
                        manager.getConnection()
                                .sync()
                                .get(key),
                executor
        );
    }

    @Override
    public CompletableFuture<Void> set(final String key, final String value) {
        return CompletableFuture.runAsync(() ->
                        manager.getConnection()
                                .sync()
                                .set(key, value),
                executor
        );
    }

    @Override
    public CompletableFuture<Void> set(final String key, final String value, final Duration ttl) {
        return CompletableFuture.runAsync(() ->
                        manager.getConnection()
                                .sync()
                                .setex(key, ttl.getSeconds(), value),
                executor
        );
    }

    @Override
    public CompletableFuture<Boolean> exists(final String key) {
        return CompletableFuture.supplyAsync(() ->
                        manager.getConnection()
                                .sync()
                                .exists(key) > 0,
                executor
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(final String key) {
        return CompletableFuture.supplyAsync(() ->
                        manager.getConnection()
                                .sync()
                                .del(key) > 0,
                executor
        );
    }

    private final Map<String, java.util.List<Consumer<String>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(final String channel, final Consumer<String> handler) {
        subscribers.computeIfAbsent(channel, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(handler);
        
        // Only add listener if it's the first subscriber for this channel
        if (subscribers.get(channel).size() == 1) {
             manager.getSubConnection().addListener(new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(String ch, String message) {
                    if(!ch.equals(channel)) return;

                    final java.util.List<Consumer<String>> consumers = subscribers.get(ch);
                    if (consumers != null) {
                        consumers.forEach(consumer -> consumer.accept(message));
                    }
                }
            });
            manager.getSubConnection().sync().subscribe(channel);
        }
    }

    @Override
    public void unsubscribe(final String channel) {
        subscribers.remove(channel);
        manager.getSubConnection().sync().unsubscribe(channel);
    }

    @Override
    public CompletableFuture<Long> publish(final String channel, final String message) {
        return CompletableFuture.supplyAsync(() ->
                        manager.getPubConnection()
                                .sync()
                                .publish(channel, message),
                executor
        );
    }
}

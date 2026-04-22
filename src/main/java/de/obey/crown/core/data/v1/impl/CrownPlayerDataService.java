package de.obey.crown.core.data.v1.impl;


/*
    Author: Obey
    Date: 02.04.2026
    Time: 10:10
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import com.j256.ormlite.dao.Dao;
import de.obey.crown.core.data.v1.api.ICrownPlayerDataService;
import de.obey.crown.core.data.v1.exception.PlayerDataNotFoundException;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class CrownPlayerDataService<T, ID> implements ICrownPlayerDataService<T, ID> {

    private final Dao<T, ID> dao;
    private final Map<ID, T> cache = Maps.newConcurrentMap();
    private final Set<ID> dirty = ConcurrentHashMap.newKeySet();
    private final Executor executor;

    protected CrownPlayerDataService(final Dao<T, ID> dao, final Executor executor) {
        this.dao = dao;
        this.executor = executor;
    }

    protected abstract T createDefault(final ID id);

    @Override
    public CompletableFuture<T> load(final ID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                T data = dao.queryForId(id);

                if (data == null) {
                    data = createDefault(id);
                    dao.create(data);
                }

                cache.put(id, data);
                return data;
            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public Optional<T> get(final ID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public T getOrThrow(final ID id) throws PlayerDataNotFoundException {
        return get(id).orElseThrow(() ->
            new PlayerDataNotFoundException("player data not loaded: " + id));
    }

    @Override
    public boolean isLoaded(final ID id) {
        return cache.containsKey(id);
    }

    @Override
    public void edit(final ID id, final Consumer<T> consumer) {
        final T data;
        try {
            data = getOrThrow(id);
        } catch (final PlayerDataNotFoundException e) {
            throw new RuntimeException(e);
        }

        consumer.accept(data);
        dirty.add(id);
    }

    @Override
    public CompletableFuture<Void> unload(final ID id) {
        return save(id).thenRun(() -> {
            cache.remove(id);
            dirty.remove(id);
        });
    }

    @Override
    public CompletableFuture<Void> save(final ID id) {
        return CompletableFuture.runAsync(() -> {
            final T data = cache.get(id);
            if (data == null) return;

            try {
                dao.createOrUpdate(data);
                dirty.remove(id);
            } catch (final SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public void saveAllAsync() {
        CompletableFuture.runAsync(() -> {
            for (final ID id : dirty) {
                final T data = cache.get(id);
                if (data == null) continue;

                try {
                    dao.createOrUpdate(data);
                } catch (final SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            dirty.clear();
        }, executor);
    }

    @Override
    public void clearCache() {
        cache.clear();
        dirty.clear();
    }
}

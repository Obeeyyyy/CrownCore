package de.obey.crown.core.data.v1.impl;


/*
    Author: Obey
    Date: 02.04.2026
    Time: 15:05
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.data.plugin.storage.PluginStorageManager;
import de.obey.crown.core.data.v1.api.ICrownPlayerSessionService;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class CrownPlayerSessionService<S extends CrownPlayerSession<S>, ID extends UUID > implements ICrownPlayerSessionService<S, ID> {

    protected final Plugin plugin;
    protected final Executor executor = CrownCore.getInstance().getExecutor();
    protected final PluginStorageManager pluginStorageManager = CrownCore.getInstance().getPluginStorageManager();

    public final Map<ID, S> sessions = Maps.newConcurrentMap();

    protected abstract S newSession(final ID id);
    protected abstract void createTables();

    public CrownPlayerSessionService(final Plugin plugin) {
        this.plugin = plugin;

        CrownCore.getInstance().getSessionServiceHandler().registerSessionService(this);
        createTables();
    }

    @Override
    public CompletableFuture<S> load(final ID id) {
        if(sessions.containsKey(id)) {
            return CompletableFuture.completedFuture(sessions.get(id));
        }

        final S session = newSession(id);
        session.setLastSeen(System.currentTimeMillis());

        return session.loadAsync();
    }

    @Override
    public CompletableFuture<S> save(ID id) {
        if(!sessions.containsKey(id))
            return CompletableFuture.completedFuture(null);

        final S session = get(id);

        session.setLastSeen(System.currentTimeMillis());

        return session.saveAsync();
    }

    @Override
    public S get(final ID id) {
        if(!sessions.containsKey(id))
            return null;

        return sessions.get(id);
    }

    @Override
    public Optional<S> getOptional(final ID id) {
        if(!sessions.containsKey(id))
            return null;

        return Optional.ofNullable(sessions.get(id));
    }

    @Override
    public CompletableFuture<Void> unload(final ID id) {
        if(!sessions.containsKey(id))
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> sessions.remove(id), executor);
    }

    @Override
    public void saveAllAsync() {
        for (final ID id : sessions.keySet())
            save(id);
    }

    @Override
    public void saveAllSync() {
        if (sessions.isEmpty()) {
            return;
        }

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        for (final S session : sessions.values()) {
            futures.add(CompletableFuture.runAsync(session::save, executor));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            CrownCore.log.warn("Timed out or error while saving sessions for plugin " + plugin.getName() + ": " + e.getMessage());
        }
    }
}

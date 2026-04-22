package de.obey.crown.core.data.v1.impl;


/*
    Author: Obey
    Date: 02.04.2026
    Time: 15:05
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import com.j256.ormlite.table.TableUtils;
import de.obey.crown.core.data.plugin.storage.PluginStorageManager;
import de.obey.crown.core.data.v1.api.ICrownPlayerSession;
import de.obey.crown.core.data.v1.api.ICrownPlayerSessionService;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        session.setPlayer(Bukkit.getPlayer(id));
        session.setLastSeen(System.currentTimeMillis());

        return session.loadAsync();
    }

    @Override
    public CompletableFuture<S> save(ID id) {
        if(!sessions.containsKey(id)) {
            return CompletableFuture.completedFuture(null);
        }

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
    public CompletableFuture<Void> unload(final ID id) {
        if(!sessions.containsKey(id))
            return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> sessions.remove(id), executor);
    }

    @Override
    public void saveAllAsync() {
        sessions.keySet()
                .stream()
                .map(this::save);
    }

    @Override
    public void saveAllSync() {
        for (final S session : sessions.values()) {
            session.save();
        }
    }
}

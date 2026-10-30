package de.obey.crown.core.data.plugin.storage.player;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.noobf.PluginConfig;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.task.CrownTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PlayerDataService {

    private final ExecutorService executor;
    private final CrownTask cleanupTask;

    @Getter
    private final Map<UUID, PlayerData> cache = Maps.newConcurrentMap();

    public PlayerDataService(final PluginConfig pluginConfig, final ExecutorService executor) {
        this.executor = executor;

        this.cleanupTask = Scheduler.runAsyncTaskTimer(CrownCore.getInstance(), () -> {
            CrownCore.log.debug("playerDataService task - cache size: " + cache.size());
            for (final PlayerData data : cache.values()) {
                final Player player = Bukkit.getPlayer(data.getUuid());
                saveAsync(data.getUuid());

                if (player != null && player.isOnline())
                    continue;

                if (data.isUnload()) {
                    cache.remove(data.getUuid());
                    continue;
                }

                if (System.currentTimeMillis() - data.getLastseen() >= pluginConfig.getDataCacheTime()) {
                    data.setUnload(true);

                    if (pluginConfig.getDataCacheTime() <= 0)
                        cache.remove(data.getUuid());
                }

            }
        }, 20 * 60, 20 * 60);
    }

    public void unloadFromCache(final UUID uuid) {
        cache.remove(uuid);
    }

    public PlayerData get(final UUID uuid) {
        if (!cache.containsKey(uuid))
            return null;

        return cache.get(uuid);
    }

    public PlayerData load(final UUID uuid) {
        final long started = System.currentTimeMillis();

        PlayerData data = cache.computeIfAbsent(uuid, k -> {
            CrownCore.log.debug("  not in cache");
            return new PlayerData(k);
        });

        data.setUnload(false);
        CrownCore.log.debug("loaded/retrieved player data for '" + uuid + "' in " + (System.currentTimeMillis() - started) + "ms");
        return data;
    }

    public CompletableFuture<PlayerData> loadAsync(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> load(uuid), executor);
    }

    public CompletableFuture<PlayerData> saveAsync(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            CrownCore.log.debug(" saving  async " + uuid.toString());

            if (!cache.containsKey(uuid))
                return null;

            return cache.get(uuid).save();
        }, executor);
    }

    public void saveAllData() {
        if (cache.isEmpty())
            return;

        for (final PlayerData data : cache.values()) {
            try {
                data.save();
            } catch (final Throwable t) {
                CrownCore.log.warn("failed to save player data for " + data.getUuid() + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    public void saveAllDataAsync() {
        executor.execute(this::saveAllData);
    }

    public void shutdown() {
        if (cleanupTask != null)
            cleanupTask.cancel();

        saveAllData();
    }

}

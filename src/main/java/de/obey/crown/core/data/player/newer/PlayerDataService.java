package de.obey.crown.core.data.player.newer;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.Log;
import de.obey.crown.core.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PlayerDataService {

    private final ExecutorService executor;
    private final Map<UUID, PlayerData> cache = Maps.newConcurrentMap();

    public PlayerDataService(ExecutorService executor) {
        this.executor = executor;

        Scheduler.runTaskTimerAsync(CrownCore.getInstance(), () -> {
            Log.debug("playerDataService task - cache size: " + cache.size());
            for (final PlayerData data : cache.values()) {
                final Player player = Bukkit.getPlayer(data.getUuid());
                saveAsync(data.getUuid());

                if(player != null && player.isOnline())
                    continue;

                if(data.isUnload()) {
                    cache.remove(data.getUuid());
                    continue;
                }

                if(System.currentTimeMillis() - data.getLastseen() >= 1000 * 60 * 60) {
                    data.setUnload(true);
                }

            }
        }, 20*60, 20*60);
    }

    public PlayerData get(final UUID uuid) {
        if(!cache.containsKey(uuid))
            return null;

        return cache.get(uuid);
    }

    public CompletableFuture<PlayerData> loadAsync(final UUID uuid) {
        final long started = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            if(cache.containsKey(uuid)) {
                Log.debug("found player in cache '" + uuid.toString() + "' in " + (System.currentTimeMillis() - started) + "ms");
                return cache.get(uuid).setUnload(false);
            }

            final PlayerData playerData = PlayerData.create(uuid);
            cache.put(uuid, playerData);

            Log.debug("loaded player data for '" + uuid.toString() + "' in " + (System.currentTimeMillis() - started) + "ms");

            return playerData;
        }, executor);
    }

    public CompletableFuture<PlayerData> saveAsync(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if(!cache.containsKey(uuid))
                return null;

            return cache.get(uuid).save();
        }, executor);
    }

    public void saveAllData() {
        if(cache.isEmpty())
            return;

        cache.values().forEach(PlayerData::save);
    }

    public void saveAllDataAsync() {
        executor.execute(this::saveAllData);
    }

}

package de.obey.crown.core.util;

/*
    Author: Obey
    Date: 25.12.2025
    Time: 18:50
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FloodgateUtil {

    private static final Map<String, UUID> CACHE = Maps.newConcurrentMap();
    public static FloodgateApi floodgateApi;

    private static boolean floodgateEnabled = false;

    public static void init() {
        floodgateEnabled = Bukkit.getPluginManager().isPluginEnabled("floodgate");

        if(floodgateEnabled)
            floodgateApi = FloodgateApi.getInstance();
    }

    public static CompletableFuture<Boolean> isBedrockPlayer(final String username) {
        if(FloodgateUtil.floodgateApi == null)
            return CompletableFuture.completedFuture(false);

        if(!username.startsWith(floodgateApi.getPlayerPrefix()))
            return CompletableFuture.completedFuture(false);

        return FloodgateUtil.floodgateApi.getUuidFor(username).thenApply(Objects::nonNull);
    }

    public static CompletableFuture<UUID> getUuidByName(String username) {
        final String name = username.trim();

        if (CACHE.containsKey(name.toLowerCase()))
            return CompletableFuture.completedFuture(CACHE.get(name.toLowerCase()));

        final Player online = Bukkit.getPlayerExact(name);
        if (online == null)
            return CompletableFuture.completedFuture(null);

        final UUID uuid = resolveFromOnlinePlayer(online);

        if(uuid == null)
            return CompletableFuture.completedFuture(null);

        CACHE.put(name.toLowerCase(), uuid);
        return CompletableFuture.completedFuture(uuid);
    }

    private static UUID resolveBedrockByNameFloodgate(String name) {
        try {
            Object player = floodgateApi.getClass()
                    .getMethod("getPlayer", String.class)
                    .invoke(floodgateApi, name);

            if (player == null) return null;

            return (UUID) player.getClass()
                    .getMethod("getCorrectUniqueId")
                    .invoke(player);

        } catch (Exception ignored) {
            return null;
        }
    }

    private static UUID resolveFromOnlinePlayer(final Player player) {
        if (!floodgateEnabled) return player.getUniqueId();

        try {
            final FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());

            if (floodgatePlayer != null)
                return floodgatePlayer.getCorrectUniqueId();

        } catch (final Exception ignored) {}

        return player.getUniqueId();
    }

}

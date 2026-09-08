package de.obey.crown.core.util;

/*
    Author: Obey
    Date: 25.12.2025
    Time: 18:50
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class FloodgateUtil {

    private final Map<String, UUID> CACHE = Maps.newConcurrentMap();
    public FloodgateApi floodgateApi;

    @Getter
    private boolean floodgateEnabled = false;

    public void initialize() {
        floodgateEnabled = Bukkit.getPluginManager().isPluginEnabled("floodgate");

        if(floodgateEnabled)
            floodgateApi = FloodgateApi.getInstance();
    }

    public String getBedrockPrefix() {
        if(!floodgateEnabled)
            return ".";

        return floodgateApi.getPlayerPrefix();
    }

    public boolean isBedrockPlayer(final Player player) {
        if (player == null) return false;

        if (floodgateEnabled && floodgateApi != null)
            return floodgateApi.isFloodgatePlayer(player.getUniqueId());

        final String prefix = getBedrockPrefix();
        return prefix != null && !prefix.isEmpty() && player.getName().startsWith(prefix);
    }

    public boolean isBedrockPlayer(final UUID uuid) {
        if (uuid == null) return false;

        if (floodgateEnabled && floodgateApi != null)
            return floodgateApi.isFloodgatePlayer(uuid);

        final Player player = Bukkit.getPlayer(uuid);
        return player != null && isBedrockPlayer(player);
    }

    public CompletableFuture<Boolean> isBedrockPlayer(final String username) {
        if(!username.startsWith(getBedrockPrefix()))
            return CompletableFuture.completedFuture(false);

        if(!floodgateEnabled)
            return CompletableFuture.completedFuture(true);

        return floodgateApi.getUuidFor(username).thenApply(Objects::nonNull);
    }

    public CompletableFuture<UUID> getUuidByName(final String username) {
        final String name = username.trim();

        if (CACHE.containsKey(name.toLowerCase()))
            return CompletableFuture.completedFuture(CACHE.get(name.toLowerCase()));

        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) {

            final UUID uuid = resolveFromOnlinePlayer(online);

            if (uuid == null)
                return CompletableFuture.completedFuture(null);

            CACHE.put(name.toLowerCase(), uuid);
            return CompletableFuture.completedFuture(uuid);
        }

        return floodgateApi.getUuidFor(name);
    }

    private UUID resolveFromOnlinePlayer(final Player player) {
        if (!floodgateEnabled) return player.getUniqueId();

        try {
            final FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());

            if (floodgatePlayer != null)
                return floodgatePlayer.getCorrectUniqueId();

        } catch (final Exception ignored) {}

        return player.getUniqueId();
    }

}

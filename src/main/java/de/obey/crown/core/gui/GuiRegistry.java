package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.gui.model.CrownGui;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public class GuiRegistry {

    private static final Map<String, CrownGui> GUIS = Maps.newConcurrentMap();
    private static final Map<String, Inventory> CACHED_INVENTORIES = Maps.newConcurrentMap();
    private static final Map<String, Map<UUID, Inventory>> PLAYER_CACHED_INVENTORIES = Maps.newConcurrentMap();

    public static void register(final CrownGui gui) {
        GUIS.put(gui.getKey(), gui);
        CACHED_INVENTORIES.remove(gui.getKey());
        PLAYER_CACHED_INVENTORIES.remove(gui.getKey());
    }

    public static CrownGui get(final String key) {
        return GUIS.get(key);
    }

    public static Map<String, CrownGui> all() {
        return GUIS;
    }

    public static void clear() {
        GUIS.clear();
        CACHED_INVENTORIES.clear();
        PLAYER_CACHED_INVENTORIES.clear();
    }

    public static Inventory getCachedInventory(final String key) {
        return CACHED_INVENTORIES.get(key);
    }

    public static void cacheInventory(final String key, final Inventory inventory) {
        CACHED_INVENTORIES.put(key, inventory);
    }

    public static Inventory getPlayerCachedInventory(final String key, final UUID uuid) {
        final Map<UUID, Inventory> playerCache = PLAYER_CACHED_INVENTORIES.get(key);
        if (playerCache == null) return null;
        return playerCache.get(uuid);
    }

    public static void cachePlayerInventory(final String key, final UUID uuid, final Inventory inventory) {
        PLAYER_CACHED_INVENTORIES.computeIfAbsent(key, k -> Maps.newConcurrentMap()).put(uuid, inventory);
    }

    public static void clearPlayerCache(final UUID uuid) {
        for (final Map<UUID, Inventory> cache : PLAYER_CACHED_INVENTORIES.values()) {
            cache.remove(uuid);
        }
    }

    public static java.util.Collection<Inventory> getCachedInventories(final String key) {
        final java.util.List<Inventory> inventories = new java.util.ArrayList<>();

        final Inventory global = CACHED_INVENTORIES.get(key);
        if (global != null) {
            inventories.add(global);
        }

        final Map<UUID, Inventory> playerCache = PLAYER_CACHED_INVENTORIES.get(key);
        if (playerCache != null) {
            inventories.addAll(playerCache.values());
        }

        return inventories;
    }
}

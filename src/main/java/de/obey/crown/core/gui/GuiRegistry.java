package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiHolder;
import de.obey.crown.core.gui.render.GuiRenderer;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiRegistry {

    private static final Map<String, CrownGui> GUIS = Maps.newConcurrentMap();
    private static final Map<String, Inventory> CACHED_INVENTORIES = Maps.newConcurrentMap();
    private static final Map<String, Map<UUID, Inventory>> PLAYER_CACHED_INVENTORIES = Maps.newConcurrentMap();

    public static void register(final CrownGui gui) {
        GUIS.put(gui.getKey(), gui);
        clearCache(gui.getKey());
        refreshOpenViewers(gui);
    }

    public static CrownGui get(final String key) {
        return GUIS.get(key);
    }

    public static Map<String, CrownGui> all() {
        return GUIS;
    }

    public static void clear() {
        GUIS.clear();
        clearCache();
    }

    public static void clearCache(final String key) {
        CACHED_INVENTORIES.remove(key);
        PLAYER_CACHED_INVENTORIES.remove(key);
    }

    public static void clearCache() {
        CACHED_INVENTORIES.clear();
        PLAYER_CACHED_INVENTORIES.clear();
    }

    public static void reloadCache(final CrownGui gui) {
        if (gui == null) return;
        clearCache(gui.getKey());

        if (gui.guiSettings().cachePerPlayer()) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                GuiRenderer.preRender(player, player, gui);
            }
        }

        refreshOpenViewers(gui);
    }

    public static void refreshOpenViewers(final CrownGui gui) {
        if (gui == null) return;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder holder) {
                    if (holder.crownGui() != null && holder.crownGui().getKey().equals(gui.getKey())) {
                        if (gui.guiSettings().cache()) {
                            final Inventory cached = getCachedInventory(gui.getKey());
                            if (cached != null) {
                                Scheduler.runEntityTask(CrownCore.getInstance(), player, () -> player.openInventory(cached));
                            }
                        } else if (gui.guiSettings().cachePerPlayer()) {
                            final Inventory playerCached = getPlayerCachedInventory(gui.getKey(), player.getUniqueId());
                            if (playerCached != null) {
                                Scheduler.runEntityTask(CrownCore.getInstance(), player, () -> player.openInventory(playerCached));
                            }
                        } else {
                            final OfflinePlayer target = holder.getTarget() != null ? holder.getTarget() : player;
                            final String[] placeholders = holder.getPlaceholders();
                            final String[] replacements = holder.getReplacements();
                            Scheduler.runEntityTask(CrownCore.getInstance(), player, () -> GuiRenderer.open(player, target, gui, placeholders, replacements));
                        }
                    }
                }
            } catch (final Exception ignored) {}
        }
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

    public static Collection<Inventory> getCachedInventories(final String key) {
        final List<Inventory> inventories = new ArrayList<>();

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

package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:09
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiHolder;
import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.gui.render.GuiRenderer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CrownGuiService {

    public static void open(final Player player, final String key) {
        final CrownGui gui = GuiRegistry.get(key);
        if (gui == null) return;
        GuiRenderer.open(player, gui);
    }

    public static void open(final Player player, final String key, final String[] placeholders, final String... replacements) {
        final CrownGui gui = GuiRegistry.get(key);
        if (gui == null) return;
        GuiRenderer.open(player, player, gui, placeholders, replacements);
    }

    public static Collection<Inventory> getCachedInventories(final String key) {
        return GuiRegistry.getCachedInventories(key);
    }

    public static Inventory getCachedInventory(final String key) {
        return GuiRegistry.getCachedInventory(key);
    }

    public static Inventory getPlayerCachedInventory(final String key, final UUID uuid) {
        return GuiRegistry.getPlayerCachedInventory(key, uuid);
    }

    public static Inventory getPlayerCachedInventory(final String key, final Player player) {
        return GuiRegistry.getPlayerCachedInventory(key, player.getUniqueId());
    }

    public static List<Integer> getDynamicSlots(final String guiKey, final String slotKey) {
        final CrownGui gui = GuiRegistry.get(guiKey);
        if (gui == null) return Collections.emptyList();
        return gui.getDynamicSlots(slotKey);
    }

    public static boolean isDynamicSlot(final String guiKey, final int slot) {
        final CrownGui gui = GuiRegistry.get(guiKey);
        return gui != null && gui.isDynamicSlot(slot);
    }

    public static Set<Integer> getAllDynamicSlots(final String guiKey) {
        final CrownGui gui = GuiRegistry.get(guiKey);
        if (gui == null) return Collections.emptySet();
        return gui.getAllDynamicSlots();
    }

    public static void setDynamicItem(final Inventory inventory, final int slot, final ItemStack itemStack, final GuiItem guiItem) {
        if (inventory != null && inventory.getHolder() instanceof GuiHolder holder) {
            holder.setDynamicItem(slot, itemStack, guiItem);
        } else if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, itemStack);
        }
    }

    public static void setDynamicItem(final Inventory inventory, final int slot, final GuiItem guiItem) {
        if (inventory != null && inventory.getHolder() instanceof GuiHolder holder) {
            holder.setDynamicItem(slot, guiItem);
        }
    }

    public static void removeDynamicItem(final Inventory inventory, final int slot) {
        if (inventory != null && inventory.getHolder() instanceof GuiHolder holder) {
            holder.removeDynamicItem(slot);
        } else if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, null);
        }
    }

    public static void reAddItems(final Inventory inventory) {
        GuiRenderer.reAddItems(inventory);
    }

    public static void clearCache(final String key) {
        GuiRegistry.clearCache(key);
    }

    public static void clearAllCaches() {
        GuiRegistry.clearCache();
    }

    public static void reloadCache(final String key) {
        final CrownGui gui = GuiRegistry.get(key);
        if (gui != null) {
            GuiRegistry.reloadCache(gui);
        }
    }
}

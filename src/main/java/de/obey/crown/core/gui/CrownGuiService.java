package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:09
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.render.GuiRenderer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    public static void reAddItems(final Inventory inventory) {
        GuiRenderer.reAddItems(inventory);
    }
}

package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:06
    Project: CrownCore
*/

import de.obey.crown.core.util.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public record GuiItem(List<Integer> slots, boolean add, ItemBuilder itemBuilder, GuiItemClickAction guiItemClickAction, String action, String permission) {

    public GuiItem(List<Integer> slots, ItemBuilder itemBuilder, GuiItemClickAction guiItemClickAction, String action, String permission) {
        this(slots, false, itemBuilder, guiItemClickAction, action, permission);
    }

    public GuiItem(List<Integer> slots, ItemBuilder itemBuilder, GuiItemClickAction guiItemClickAction, String permission) {
        this(slots, false, itemBuilder, guiItemClickAction, null, permission);
    }

    public GuiItem(int slot, ItemBuilder itemBuilder, GuiItemClickAction guiItemClickAction, String permission) {
        this(Collections.singletonList(slot), false, itemBuilder, guiItemClickAction, null, permission);
    }

    public int slot() {
        return slots.isEmpty() ? -1 : slots.get(0);
    }

    public boolean canView(final Player player) {
        return permission == null || player.hasPermission(permission);
    }

}

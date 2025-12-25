package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:06
    Project: CrownCore
*/

import de.obey.crown.core.util.ItemBuilder;
import org.bukkit.entity.Player;

public record GuiItem(int slot, ItemBuilder itemBuilder, GuiItemClickAction guiItemClickAction, String permission) {

    public boolean canView(final Player player) {
        return permission == null || player.hasPermission(permission);
    }

}

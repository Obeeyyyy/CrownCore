package de.obey.crown.core.gui;

import de.obey.crown.core.gui.model.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface GuiAction {
    void execute(Player player, GuiItem item, InventoryClickEvent event);
}

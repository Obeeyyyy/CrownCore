package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:01
    Project: CrownCore
*/

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public record GuiHolder(CrownGui crownGui) implements InventoryHolder {

    @Override
    public Inventory getInventory() {
        return null;
    }
}

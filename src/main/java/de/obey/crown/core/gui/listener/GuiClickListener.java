package de.obey.crown.core.gui.listener;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:16
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.GuiHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickListener implements Listener {

    @EventHandler
    public void on (final InventoryClickEvent event) {
        if(event.getView().getTopInventory().getHolder() instanceof GuiHolder){
            event.setCancelled(true);
        }
    }
}

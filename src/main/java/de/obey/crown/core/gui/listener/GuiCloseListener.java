package de.obey.crown.core.gui.listener;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:29
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiCloseListener implements Listener {

    @EventHandler
    public void on (final InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuiHolder holder))
            return;

        final CrownGui gui = holder.crownGui();
        if (gui == null) return;

        final SoundData closeSound = gui.guiSettings().closeSound();
        if (closeSound != null) {
            final Player player = (Player) event.getPlayer();
            player.playSound(player.getLocation(), closeSound.getSound(), closeSound.getVolume(), closeSound.getPitch());
        }
    }


}

package de.obey.crown.core.gui.listener;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:16
    Project: CrownCore
*/

import de.obey.crown.core.gui.GuiAction;
import de.obey.crown.core.gui.GuiActionRegistry;
import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiHolder;
import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.gui.model.GuiItemClickAction;
import de.obey.crown.core.gui.render.GuiRenderer;
import de.obey.crown.core.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickListener implements Listener {

    @EventHandler
    public void on(final InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder holder))
            return;

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory())
            return;

        final int slot = event.getSlot();

        if (slot < 0)
            return;

        final CrownGui gui = holder.crownGui();
        if (gui == null)
            return;

        final GuiItem clickedItem = holder.getItemLayout().get(slot);

        if (clickedItem == null)
            return;

        final Player player = (Player) event.getWhoClicked();
        if (!clickedItem.canView(player))
            return;

        final String customAction = clickedItem.action();
        if (customAction != null) {
            final GuiAction guiAction = GuiActionRegistry.get(customAction);
            if (guiAction != null) {
                guiAction.execute(player, clickedItem, event);
            }
        }

        final GuiItemClickAction action = clickedItem.guiItemClickAction();
        if (action == null) {
            return;
        }
        if (action.close() || action.type() == GuiItemClickAction.Type.CLOSE)
            player.closeInventory();

        switch (action.type()) {
            case OPEN_GUI -> {
                if (action.value() != null) {
                    GuiRenderer.open(player, action.value());
                }
            }
            case COMMAND -> {
                if (action.value() != null) {
                    String command = PlaceholderUtil.resolve(player, action.value());
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }
                    player.performCommand(command);
                }
            }
            case CONSOLE_COMMAND -> {
                if (action.value() != null) {
                    String command = PlaceholderUtil.resolve(player, action.value());
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }
}

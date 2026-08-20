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
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.PlaceholderUtil;
import de.obey.crown.core.util.Scheduler;
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

        if (event.isLeftClick()) {
            final String customAction = clickedItem.action();
            if (customAction != null) {
                final GuiAction guiAction = GuiActionRegistry.get(customAction);
                if (guiAction != null) {
                    guiAction.execute(player, clickedItem, event);
                }
            }

            handleClickAction(player, clickedItem.guiItemClickAction());
        } else if (event.isRightClick()) {
            handleClickAction(player, clickedItem.rightClickAction());
        }
    }

    private void handleClickAction(final Player player, final GuiItemClickAction action) {
        if (action == null || action.type() == GuiItemClickAction.Type.NONE) {
            return;
        }

        if (action.type() != GuiItemClickAction.Type.OPEN_GUI && (action.close() || action.type() == GuiItemClickAction.Type.CLOSE)) {
            player.closeInventory();
        }

        if (action.type() == GuiItemClickAction.Type.CLOSE) {
            return;
        }

        if (!player.isOnline() || !player.isValid()) {
            return;
        }

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
                    final String finalCommand = command;
                    Scheduler.runEntityTask(CrownCore.getInstance(), player, () -> player.performCommand(finalCommand));
                }
            }
            case CONSOLE_COMMAND -> {
                if (action.value() != null) {
                    String command = PlaceholderUtil.resolve(player, action.value());
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }
                    final String finalCommand = command;
                    Scheduler.runGlobalTask(CrownCore.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
                }
            }
        }
    }
}

package de.obey.crown.core.gui.render;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:11
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.gui.GuiRegistry;
import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiHolder;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.PlaceholderUtil;
import de.obey.crown.core.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiRenderer {

    public static void open(final Player player, final CrownGui gui) {
        open(player, player, gui);
    }

    public static void open(final Player player, final String key) {
        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            CrownCore.getInstance().getMessanger().sendNonConfigMessage(player, "%prefix% unknown gui: " + key);
            CrownCore.log.warn("unknown gui " + key);
            return;
        }

        open(player, player, gui);
    }


    public static void open(final Player player, final OfflinePlayer target, final String key) {

        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            CrownCore.getInstance().getMessanger().sendNonConfigMessage(player, "%prefix% unknown gui: " + key);
            CrownCore.log.warn("unknown gui " + key);
            return;
        }

        open(player, target, gui);
    }

    public static void open(final Player player, final OfflinePlayer target, final CrownGui gui) {
        final Inventory inventory = Bukkit.createInventory(
                new GuiHolder(gui),
                gui.size(),
                TextUtil.translateColors(PlaceholderUtil.resolve(target, gui.title()))
        );

        applyFill(target, inventory, gui);

        gui.items().values().forEach(item -> {
            if (!item.canView(player)) return;
            inventory.setItem(item.slot(), item.itemBuilder().build(target));
        });

        final SoundData openSound = gui.guiSettings().openSound();
        if (openSound != null) {
            player.playSound(player.getLocation(), openSound.getSound(), openSound.getVolume(), openSound.getPitch());
        }

        player.openInventory(inventory);
    }

    private static void applyFill(final OfflinePlayer player, final Inventory inventory, final CrownGui gui) {
        final GuiFill fill = gui.guiSettings().fill();
        if (!fill.enabled()) return;

        final ItemStack fillItem = fill.item().build(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillItem);
            }
        }
    }

}

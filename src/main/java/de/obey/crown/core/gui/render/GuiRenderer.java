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
import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.noobf.CrownCore;
import java.util.Map;
import de.obey.crown.core.util.ItemBuilder;
import de.obey.crown.core.util.PlaceholderUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiRenderer {

    public static void open(final Player player, final CrownGui gui) {
        open(player, player, gui, null);
    }

    public static void open(final Player player, final String key) {
        open(player, player, key, null);
    }

    public static void open(final Player player, final OfflinePlayer target, final String key) {
        open(player, target, key, null);
    }

    public static void preRender(final Player player, final OfflinePlayer target, final String key) {
        preRender(player, target, key, null);
    }

    public static void open(final Player player, final OfflinePlayer target, final CrownGui gui) {
        open(player, target, gui, null);
    }

    public static void preRender(final Player player, final OfflinePlayer target, final CrownGui gui) {
        preRender(player, target, gui, null);
    }

    public static void open(final Player player, final String key, final String[] placeholders, final String... replacements) {
        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            CrownCore.getInstance().getMessanger().sendNonConfigMessage(player, "%prefix% unknown gui: " + key);
            CrownCore.log.warn("unknown gui " + key);
            return;
        }

        open(player, player, gui, placeholders, replacements);
    }

    public static void open(final Player player, final OfflinePlayer target, final String key, final String[] placeholders, final String... replacements) {
        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            CrownCore.getInstance().getMessanger().sendNonConfigMessage(player, "%prefix% unknown gui: " + key);
            CrownCore.log.warn("unknown gui " + key);
            return;
        }

        open(player, target, gui, placeholders, replacements);
    }

    public static void preRender(final Player player, final OfflinePlayer target, final String key, final String[] placeholders, final String... replacements) {
        final CrownGui gui = GuiRegistry.get(key);

        if (gui == null) {
            CrownCore.getInstance().getMessanger().sendNonConfigMessage(player, "%prefix% unknown gui: " + key);
            CrownCore.log.warn("unknown gui " + key);
            return;
        }

        preRender(player, target, gui, placeholders, replacements);
    }

    public static void open(final Player player, final OfflinePlayer target, final CrownGui gui, final String[] placeholders, final String... replacements) {
        Inventory inventory = null;
        if (gui.guiSettings().cache()) {
            inventory = GuiRegistry.getCachedInventory(gui.getKey());
        } else if (gui.guiSettings().cachePerPlayer()) {
            inventory = GuiRegistry.getPlayerCachedInventory(gui.getKey(), target.getUniqueId());
        }

        if (inventory == null) {
            String title = gui.title();
            if (placeholders != null && replacements != null) {
                for (int i = 0; i < placeholders.length; i++) {
                    title = title.replace("%" + placeholders[i] + "%", replacements[i]);
                }
            }

            inventory = Bukkit.createInventory(
                    new GuiHolder(gui),
                    gui.size(),
                    MiniMessage.miniMessage().deserialize(PlaceholderUtil.resolve(target, title))
            );
            ((GuiHolder) inventory.getHolder()).setInventory(inventory);
            ((GuiHolder) inventory.getHolder()).setRenderState(target, placeholders, replacements);

            applyFill(target, inventory, gui, placeholders, replacements);

            final Inventory finalInventory = inventory;
            final GuiHolder holder = (GuiHolder) inventory.getHolder();
            holder.getItemLayout().clear();

            // Phase 1: Fixed items
            gui.items().values().forEach(item -> {
                if (item.add()) return;
                if (!item.canView(player)) return;

                final ItemBuilder builder = item.itemBuilder().clone();
                builder.resolvePlaceholders(placeholders, replacements);
                final ItemStack itemStack = builder.build(target);
                for (final int slot : item.slots()) {
                    finalInventory.setItem(slot, itemStack);
                    holder.getItemLayout().put(slot, item);
                }
            });

            // Phase 2: Add items
            gui.items().values().forEach(item -> {
                if (!item.add()) return;
                if (!item.canView(player)) return;

                final ItemBuilder builder = item.itemBuilder().clone();
                builder.resolvePlaceholders(placeholders, replacements);
                final ItemStack itemStack = builder.build(target);
                final int emptySlot = finalInventory.firstEmpty();
                if (emptySlot != -1) {
                    finalInventory.setItem(emptySlot, itemStack);
                    holder.getItemLayout().put(emptySlot, item);
                }
            });

            if (gui.guiSettings().cache()) {
                GuiRegistry.cacheInventory(gui.getKey(), inventory);
            } else if (gui.guiSettings().cachePerPlayer()) {
                GuiRegistry.cachePlayerInventory(gui.getKey(), target.getUniqueId(), inventory);
            }
        }

        final SoundData openSound = gui.guiSettings().openSound();
        if (openSound != null)
            player.playSound(player.getLocation(), openSound.getSound(), openSound.getVolume(), openSound.getPitch());

        player.openInventory(inventory);
    }

    public static void preRender(final Player player, final OfflinePlayer target, final CrownGui gui, final String[] placeholders, final String... replacements) {
        if (gui.guiSettings().cache() && GuiRegistry.getCachedInventory(gui.getKey()) != null)
            return;
        if (gui.guiSettings().cachePerPlayer() && GuiRegistry.getPlayerCachedInventory(gui.getKey(), target.getUniqueId()) != null)
            return;

        String title = gui.title();
        if (placeholders != null && replacements != null) {
            for (int i = 0; i < placeholders.length; i++) {
                title = title.replace("%" + placeholders[i] + "%", replacements[i]);
            }
        }

        final Inventory inventory = Bukkit.createInventory(
                new GuiHolder(gui),
                gui.size(),
                MiniMessage.miniMessage().deserialize(PlaceholderUtil.resolve(target, title))
        );
        ((GuiHolder) inventory.getHolder()).setInventory(inventory);
        ((GuiHolder) inventory.getHolder()).setRenderState(target, placeholders, replacements);

        applyFill(target, inventory, gui, placeholders, replacements);

        final GuiHolder holder = (GuiHolder) inventory.getHolder();
        holder.getItemLayout().clear();

        // Phase 1: Fixed items
        gui.items().values().forEach(item -> {
            if (item.add()) return;
            if (!item.canView(player)) return;

            final ItemBuilder builder = item.itemBuilder().clone();
            builder.resolvePlaceholders(placeholders, replacements);
            final ItemStack itemStack = builder.build(target);
            for (final int slot : item.slots()) {
                inventory.setItem(slot, itemStack);
                holder.getItemLayout().put(slot, item);
            }
        });

        // Phase 2: Add items
        gui.items().values().forEach(item -> {
            if (!item.add()) return;
            if (!item.canView(player)) return;

            final ItemBuilder builder = item.itemBuilder().clone();
            builder.resolvePlaceholders(placeholders, replacements);
            final ItemStack itemStack = builder.build(target);
            final int emptySlot = inventory.firstEmpty();
            if (emptySlot != -1) {
                inventory.setItem(emptySlot, itemStack);
                holder.getItemLayout().put(emptySlot, item);
            }
        });

        if (gui.guiSettings().cache()) {
            GuiRegistry.cacheInventory(gui.getKey(), inventory);
        } else if (gui.guiSettings().cachePerPlayer()) {
            GuiRegistry.cachePlayerInventory(gui.getKey(), target.getUniqueId(), inventory);
        }
    }

    private static void applyFill(final OfflinePlayer player, final Inventory inventory, final CrownGui gui, final String[] placeholders, final String... replacements) {
        final GuiFill fill = gui.guiSettings().fill();
        if (!fill.enabled()) return;

        final ItemBuilder builder = fill.item().clone();
        builder.resolvePlaceholders(placeholders, replacements);
        final ItemStack fillItem = builder.build(player);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillItem);
            }
        }
    }

    public static void reAddItems(final Inventory inventory) {
        if (!(inventory.getHolder() instanceof GuiHolder holder)) return;
        final CrownGui gui = holder.crownGui();
        if (gui == null) return;

        final Map<Integer, GuiItem> layout = holder.getItemLayout();
        layout.entrySet().removeIf(entry -> {
            final int slot = entry.getKey();
            final GuiItem item = entry.getValue();
            if (item.add()) {
                inventory.setItem(slot, null);
                return true;
            }
            return false;
        });

        final Player player = inventory.getViewers().isEmpty() ? null : (Player) inventory.getViewers().get(0);

        gui.items().values().forEach(item -> {
            if (!item.add()) return;
            if (player != null && !item.canView(player)) return;

            final ItemBuilder builder = item.itemBuilder().clone();
            builder.resolvePlaceholders(holder.getPlaceholders(), holder.getReplacements());
            final ItemStack itemStack = builder.build(holder.getTarget());
            final int emptySlot = inventory.firstEmpty();
            if (emptySlot != -1) {
                inventory.setItem(emptySlot, itemStack);
                layout.put(emptySlot, item);
            }
        });
    }

}

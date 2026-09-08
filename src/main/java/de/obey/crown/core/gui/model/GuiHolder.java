package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:01
    Project: CrownCore
*/

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GuiHolder implements InventoryHolder {

    private final CrownGui crownGui;
    private final Map<Integer, GuiItem> itemLayout = new HashMap<>();
    private Inventory inventory;
    private OfflinePlayer target;
    private String[] placeholders;
    private String[] replacements;

    public GuiHolder(final CrownGui crownGui) {
        this.crownGui = crownGui;
    }

    public CrownGui crownGui() {
        return crownGui;
    }

    public Map<Integer, GuiItem> getItemLayout() {
        return itemLayout;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    public void setDynamicItem(final int slot, final ItemStack itemStack, final GuiItem item) {
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, itemStack);
        }
        if (item != null) {
            itemLayout.put(slot, item);
        } else {
            itemLayout.remove(slot);
        }
    }

    public void setDynamicItem(final int slot, final GuiItem item) {
        if (item == null) {
            removeDynamicItem(slot);
            return;
        }
        itemLayout.put(slot, item);
        if (inventory != null && slot >= 0 && slot < inventory.getSize() && item.itemBuilder() != null) {
            final ItemStack stack = item.itemBuilder().clone().build(target);
            inventory.setItem(slot, stack);
        }
    }

    public void removeDynamicItem(final int slot) {
        itemLayout.remove(slot);
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, null);
        }
    }

    public void setRenderState(final OfflinePlayer target, final String[] placeholders, final String... replacements) {
        this.target = target;
        this.placeholders = placeholders;
        this.replacements = replacements;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public String[] getPlaceholders() {
        return placeholders;
    }

    public String[] getReplacements() {
        return replacements;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

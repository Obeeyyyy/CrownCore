package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 11:01
    Project: CrownCore
*/

import de.obey.crown.core.gui.GuiRegistry;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

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

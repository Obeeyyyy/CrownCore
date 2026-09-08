package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:01
    Project: CrownCore
*/

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CrownGui(String pluginName, String id, String title, int size, GuiSettings guiSettings,
                       Map<String, GuiItem> items, Map<String, List<Integer>> dynamicSlots) {

    public CrownGui(String pluginName, String id, String title, int size, GuiSettings guiSettings,
                    Map<String, GuiItem> items) {
        this(pluginName, id, title, size, guiSettings, items, Collections.emptyMap());
    }

    public List<Integer> getDynamicSlots(final String key) {
        return dynamicSlots.getOrDefault(key, Collections.emptyList());
    }

    public boolean isDynamicSlot(final int slot) {
        if (dynamicSlots == null || dynamicSlots.isEmpty()) {
            return false;
        }
        for (final List<Integer> slots : dynamicSlots.values()) {
            if (slots != null && slots.contains(slot)) {
                return true;
            }
        }
        return false;
    }

    public Set<Integer> getAllDynamicSlots() {
        if (dynamicSlots == null || dynamicSlots.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Integer> allSlots = new HashSet<>();
        for (final List<Integer> slots : dynamicSlots.values()) {
            if (slots != null) {
                allSlots.addAll(slots);
            }
        }
        return allSlots;
    }

    public String getKey() {
        return pluginName + ":" + id;
    }
}

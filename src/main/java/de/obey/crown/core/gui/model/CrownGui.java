package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:01
    Project: CrownCore
*/

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CrownGui(String pluginName, String id, String title, int size, GuiSettings guiSettings,
                       Map<String, GuiItem> items, Map<String, List<Integer>> dynamicSlots) {

    public CrownGui(String pluginName, String id, String title, int size, GuiSettings guiSettings,
                    Map<String, GuiItem> items) {
        this(pluginName, id, title, size, guiSettings, items, Collections.emptyMap());
    }

    public List<Integer> getDynamicSlots(final String key) {
        return dynamicSlots.getOrDefault(key, Collections.emptyList());
    }

    public String getKey() {
        return pluginName + ":" + id;
    }
}

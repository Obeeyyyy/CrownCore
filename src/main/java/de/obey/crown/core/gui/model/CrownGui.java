package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:01
    Project: CrownCore
*/

import java.util.Map;

public record CrownGui(String pluginName, String id, String title, int size, GuiSettings guiSettings,
                       Map<String, GuiItem> items) {

    public String getKey() {
        return pluginName + ":" + id;
    }
}

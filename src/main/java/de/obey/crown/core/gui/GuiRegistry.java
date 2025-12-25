package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.gui.model.CrownGui;

import java.util.Map;

public class GuiRegistry {

    private static final Map<String, CrownGui> GUIS = Maps.newConcurrentMap();

    public static void register(final CrownGui gui) {
        GUIS.put(gui.getKey(), gui);
    }

    public static CrownGui get(final String key) {
        return GUIS.get(key);
    }

    public static Map<String, CrownGui> all() {
        return GUIS;
    }

    public static void clear() {
        GUIS.clear();
    }
}

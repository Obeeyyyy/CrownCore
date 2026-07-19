package de.obey.crown.core.gui;

import com.google.common.collect.Maps;

import java.util.Map;

public class GuiActionRegistry {

    private static final Map<String, GuiAction> ACTIONS = Maps.newConcurrentMap();

    public static void register(final String key, final GuiAction action) {
        ACTIONS.put(key.toLowerCase(), action);
    }

    public static GuiAction get(final String key) {
        if (key == null) return null;
        return ACTIONS.get(key.toLowerCase());
    }

    public static void unregister(final String key) {
        ACTIONS.remove(key.toLowerCase());
    }

    public static void clear() {
        ACTIONS.clear();
    }
}

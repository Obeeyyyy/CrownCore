package de.obey.crown.core.gui.util;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:11
    Project: CrownCore
*/

import de.obey.crown.core.noobf.CrownCore;
import org.bukkit.configuration.ConfigurationSection;

public class GuiValidation {

    public static void validateSize(final String fileName, final int size) {
        if (size <= 0 || size % 9 != 0 || size > 54) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Invalid inventory size in " + fileName +
                            " (must be a multiple of 9 between 9 and 54)"
            );
        }
    }

    public static void validateSlot(final String guiKey, final String itemKey, final int slot, final int size) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Invalid slot " + slot +
                            " for item '" + itemKey +
                            "' in GUI " + guiKey +
                            " (size=" + size + ")"
            );
        }
    }

    public static void require(
            final ConfigurationSection section,
            final String path,
            final String context
    ) {
        if (!section.contains(path)) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Missing required field '" + path +
                            "' in " + context
            );
        }
    }

    public static void warn(final String message) {
        CrownCore.log.warn(message);
    }
}

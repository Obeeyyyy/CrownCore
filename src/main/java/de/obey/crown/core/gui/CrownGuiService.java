package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:09
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.render.GuiRenderer;
import org.bukkit.entity.Player;

public class CrownGuiService {

    public static void open(final Player player, final String key) {
        final CrownGui gui = GuiRegistry.get(key);
        if (gui == null) return;
        GuiRenderer.open(player, gui);
    }
}

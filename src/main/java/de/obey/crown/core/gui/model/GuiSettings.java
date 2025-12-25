package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.gui.render.GuiFill;
import org.bukkit.Sound;

public record GuiSettings(SoundData openSound, SoundData closeSound, int updateInterval, GuiFill fill) { }

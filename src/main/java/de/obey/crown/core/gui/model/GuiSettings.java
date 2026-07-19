package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.gui.render.GuiFill;

import java.util.Collections;
import java.util.List;

public record GuiSettings(SoundData openSound, SoundData closeSound, int updateInterval, GuiFill fill, boolean cache, boolean cachePerPlayer, List<String> defaultFlags) {

    public GuiSettings(SoundData openSound, SoundData closeSound, int updateInterval, GuiFill fill, boolean cache, boolean cachePerPlayer) {
        this(openSound, closeSound, updateInterval, fill, cache, cachePerPlayer, Collections.emptyList());
    }

    public GuiSettings(SoundData openSound, SoundData closeSound, int updateInterval, GuiFill fill, boolean cache) {
        this(openSound, closeSound, updateInterval, fill, cache, false, Collections.emptyList());
    }

    public GuiSettings(SoundData openSound, SoundData closeSound, int updateInterval, GuiFill fill) {
        this(openSound, closeSound, updateInterval, fill, false, false, Collections.emptyList());
    }
}

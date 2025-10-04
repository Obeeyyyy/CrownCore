/* CrownPlugins - CrownCore */
/* 21.04.2025 - 17:56 */

package de.obey.crown.core.data.plugin.sound;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;

@Getter
@Setter
public final class SoundData {

    private String sound;
    private float volume = 1f, pitch = 1f;
}

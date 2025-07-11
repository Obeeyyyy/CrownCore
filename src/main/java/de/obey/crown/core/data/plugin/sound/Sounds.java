/* CrownPlugins - CrownCore */
/* 21.04.2025 - 17:53 */

package de.obey.crown.core.data.plugin.sound;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public final class Sounds {

    private final Plugin plugin;

    private final Map<String, SoundData> sounds = Maps.newConcurrentMap();

    public void load() {
        final File file = FileUtil.getGeneratedFile(plugin, "sounds.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if (configuration.contains("sounds")) {
            for (final String key : configuration.getConfigurationSection("sounds").getKeys(false)) {
                final String value = configuration.getString("sounds." + key);
                if (value == null || value.isBlank())
                    continue;

                final String[] data = value.split(":");

                try {
                    final Sound sound = Sound.valueOf(data[0]);
                    final SoundData soundData = new SoundData();

                    soundData.setSound(sound);

                    if (data.length > 1) {
                        try {
                            final float volume = Float.parseFloat(data[1]);
                            soundData.setVolume(volume);
                        } catch (final NumberFormatException exception) {
                           CrownCore.log.warn("Invalid sound volume value @ " + key);
                        }
                    }

                    if (data.length > 2) {
                        try {
                            final float pitch = Float.parseFloat(data[2]);
                            soundData.setPitch(pitch);
                        } catch (final NumberFormatException exception) {
                            CrownCore.log.warn("Invalid sound pitch value @ " + key);
                        }
                    }
                    
                    sounds.put(key, soundData);
                } catch (final IllegalArgumentException exception) {
                    CrownCore.log.warn("Invalid sound @ " + key);
                }
            }
        }
    }

    private void generateMessageEntryIfMissing(final String key) {
        if (!sounds.containsKey(key)) {
            final File file = FileUtil.getGeneratedFile(plugin, "sounds.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            String value = "ENTITY_GENERIC_EXPLODE:0.5:3.0";
            configuration.set("sounds." + key, value);

            final SoundData soundData = new SoundData();
            soundData.setSound(Sound.ENTITY_GENERIC_EXPLODE);
            soundData.setVolume(0.5f);
            soundData.setPitch(3f);

            sounds.put(key, soundData);

            FileUtil.saveConfigurationIntoFile(configuration, file);
        }
    }

    public SoundData getSoundData(final String key) {
        return sounds.get(key);
    }

    public void playSoundToPlayerAtLocation(final Player player, final String key, final Location location) {
        generateMessageEntryIfMissing(key);

        final SoundData soundData = sounds.get(key);

        player.playSound(location, soundData.getSound(), soundData.getVolume(), soundData.getPitch());
    }

    public void playSoundToPlayer(final Player player, final String key) {
        playSoundToPlayerAtLocation(player, key, player.getLocation());
    }

    public void playSoundToEveryoneAtLocation(final Location location, final String key) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        for (final Player all : Bukkit.getOnlinePlayers()) {
            if (all.getLocation().getWorld() != location.getWorld())
                continue;

            playSoundToPlayerAtLocation(all, key, location);
        }
    }

    public void playSoundToEveryone(final String key) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        for (final Player all : Bukkit.getOnlinePlayers()) {
            playSoundToPlayer(all, key);
        }
    }

}

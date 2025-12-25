package de.obey.crown.core.gui;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.gui.model.CrownGui;
import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.gui.model.GuiSettings;
import de.obey.crown.core.gui.render.GuiFill;
import de.obey.crown.core.gui.util.GuiItemParser;
import de.obey.crown.core.gui.util.GuiValidation;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GuiLoader {

    public static void loadAll(final Plugin plugin) {
        final File guiFolder = new File(plugin.getDataFolder(), "gui");

        if (!guiFolder.exists()) {
            extractGuiResources(plugin, guiFolder);
        }

        if (!guiFolder.exists()) return;

        final File[] files = guiFolder.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".yml"));

        if (files == null) return;

        for (final File file : files) {
            load(plugin, file);
        }
    }

    private static void load(final Plugin plugin, final File file) {
        CrownCore.getInstance().getExecutor().execute(() -> {
            CrownCore.log.info("loading gui for plugin " + plugin.getName() + ": " + file.getName());

            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            final String id = file.getName().split("\\.")[0];
            final String title = FileUtil.getString(configuration, "title", "Default Title");
            final int size = FileUtil.getInt(configuration, "size", 27);

            GuiValidation.validateSize(file.getName(), size);

            final Map<String, GuiItem> items = new HashMap<>();
            final ConfigurationSection section = configuration.getConfigurationSection("items");

            if (section != null) {
                for (final String key : section.getKeys(false)) {
                    items.put(key, GuiItemParser.parse(section.getConfigurationSection(key), plugin.getName() + ":" + id, size));
                }
            }

            final GuiSettings guiSettings = parseSettings(configuration);
            final CrownGui gui = new CrownGui(
                    plugin.getName(),
                    id,
                    title,
                    size,
                    guiSettings,
                    items
            );

            GuiRegistry.register(gui);
        });
    }

    private static GuiSettings parseSettings(
            final YamlConfiguration cfg
    ) {

        final SoundData openSoundData = parseSoundData(cfg.getString("open-sound"));
        final SoundData closeSoundData = parseSoundData(cfg.getString("close-sound"));
        final int updateInterval = cfg.getInt("update-interval", -1);

        final GuiFill fill = parseFill(cfg.getConfigurationSection("fill"));

        return new GuiSettings(
                openSoundData,
                closeSoundData,
                updateInterval,
                fill
        );
    }

    private static SoundData parseSoundData(final String value) {
        final String[] data = value.split(":");
        final SoundData soundData = new SoundData();
        final int indexOffset = data[0].equalsIgnoreCase("minecraft") ? 1 : 0;

        try {
            final Sound sound = Sound.valueOf(data[0]);

            soundData.setSound(sound.getKey().toString());

        } catch (final IllegalArgumentException exception) {
            soundData.setSound(data[0] + ":" + data[1]);
        }

        if (data.length > 1) {
            try {
                final float volume = Float.parseFloat(data[1 + indexOffset]);
                soundData.setVolume(volume);
            } catch (final NumberFormatException exception) {
                CrownCore.log.warn("Invalid sound volume value @ " + value);
            }
        }

        if (data.length > 2) {
            try {
                final float pitch = Float.parseFloat(data[2 + indexOffset]);
                soundData.setPitch(pitch);
            } catch (final NumberFormatException exception) {
                CrownCore.log.warn("Invalid sound pitch value @ " + value);
            }
        }

        return soundData;
    }

    private static GuiFill parseFill(final ConfigurationSection section) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return new GuiFill(false, null);
        }

        final Material material = Material.matchMaterial(
                section.getString("material", "")
        );

        if (material == null) {
            CrownCore.log.warn("[CrownGUI] Invalid fill material");
            return new GuiFill(false, null);
        }

        final ItemBuilder builder = new ItemBuilder(material)
                .name(section.getString("name", " "));

        return new GuiFill(true, builder);
    }

    private static void extractGuiResources(
            final Plugin plugin,
            final File targetFolder
    ) {
        try {
            final URL resourceUrl = plugin.getClass()
                    .getClassLoader()
                    .getResource("gui");

            if (resourceUrl == null) return;

            CrownCore.log.info("extracting GUI resources for " + plugin.getName());

            targetFolder.mkdirs();

            copyFolderFromJar(plugin, "gui", targetFolder);

        } catch (final Exception ex) {
            CrownCore.log.warn("failed to extract GUI resources for " + plugin.getName());
            ex.printStackTrace();
        }
    }

    private static void copyFolderFromJar(
            final Plugin plugin,
            final String jarPath,
            final File targetFolder
    ) throws IOException, URISyntaxException {

        final CodeSource source = plugin.getClass()
                .getProtectionDomain()
                .getCodeSource();

        if (source == null) return;

        final URL jar = source.getLocation();
        final JarFile jarFile = new JarFile(new File(jar.toURI()));

        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();

            final String name = entry.getName();
            if (!name.startsWith(jarPath + "/")) continue;

            final String relative = name.substring(jarPath.length() + 1);
            if (relative.isEmpty()) continue;

            File outFile = new File(targetFolder, relative);

            if (entry.isDirectory()) {
                outFile.mkdirs();
                continue;
            }

            if (outFile.exists()) continue;

            outFile.getParentFile().mkdirs();

            try (final InputStream in = jarFile.getInputStream(entry);
                 final OutputStream out = new FileOutputStream(outFile)) {

                in.transferTo(out);
            }
        }

        jarFile.close();
    }


}

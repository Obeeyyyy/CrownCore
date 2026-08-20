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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GuiLoader {

    public static void loadAll(final Plugin plugin) {
        CrownCore.log.debug("[GuiLoader] loadAll called for plugin: " + plugin.getName());
        final File guiFolder = new File(plugin.getDataFolder(), "gui");

        extractGuiResources(plugin, guiFolder);

        CrownCore.log.debug("[GuiLoader] guiFolder exists for " + plugin.getName() + ": " + guiFolder.exists() + " (" + guiFolder.getAbsolutePath() + ")");
        if (!guiFolder.exists()) return;

        final File[] files = guiFolder.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".yml"));

        if (files == null) {
            CrownCore.log.debug("[GuiLoader] listFiles returned null for " + plugin.getName());
            return;
        }

        CrownCore.log.debug("[GuiLoader] Found " + files.length + " GUI file(s) for " + plugin.getName());
        for (final File file : files)
            load(plugin, file);
    }

    private static void load(final Plugin plugin, final File file) {
        CrownCore.getInstance().getExecutor().execute(() -> {
            CrownCore.log.debug("loading gui for plugin " + plugin.getName() + ": " + file.getName());

            try {
                final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                final String id = file.getName().split("\\.")[0];
                final String guiKey = plugin.getName() + ":" + id;
                final String title = FileUtil.getString(configuration, "title", "Default Title");
                final int size = FileUtil.getInt(configuration, "size", 27);

                GuiValidation.validateSize(file.getName(), size);

                final GuiSettings guiSettings = parseSettings(configuration, guiKey);

                final Map<String, GuiItem> items = new HashMap<>();
                final ConfigurationSection section = configuration.getConfigurationSection("items");

                if (section != null) {
                    for (final String key : section.getKeys(false)) {
                        final ConfigurationSection itemSection = section.getConfigurationSection(key);
                        if (itemSection == null) continue;
                        try {
                            final GuiItem item = GuiItemParser.parse(itemSection, guiKey, size, guiSettings.defaultFlags());
                            if (item != null) {
                                items.put(key, item);
                            }
                        } catch (final Exception ex) {
                            CrownCore.log.warn("[CrownGUI] Failed to parse item '" + key + "' in GUI " + guiKey + ": " + ex.getMessage());
                        }
                    }
                }

                final Map<String, List<Integer>> dynamicSlots = new HashMap<>();
                final ConfigurationSection dynamicSection = configuration.getConfigurationSection("dynamic-slots");
                if (dynamicSection != null) {
                    for (final String key : dynamicSection.getKeys(false)) {
                        try {
                            final List<Integer> list = dynamicSection.getIntegerList(key);
                            final List<Integer> validSlots = new java.util.ArrayList<>();
                            for (final int slot : list) {
                                if (GuiValidation.validateSlot(guiKey, "dynamic-slots." + key, slot, size)) {
                                    validSlots.add(slot);
                                }
                            }
                            dynamicSlots.put(key, validSlots);
                        } catch (final Exception ex) {
                            CrownCore.log.warn("[CrownGUI] Failed to parse dynamic slot '" + key + "' in GUI " + guiKey + ": " + ex.getMessage());
                        }
                    }
                }

                final CrownGui gui = new CrownGui(
                        plugin.getName(),
                        id,
                        title,
                        size,
                        guiSettings,
                        items,
                        dynamicSlots
                );

                GuiRegistry.register(gui);
            } catch (final Exception ex) {
                CrownCore.log.warn("[CrownGUI] Failed to load GUI from file '" + file.getName() + "' for plugin " + plugin.getName() + ": " + ex.getMessage());
            }
        });
    }

    private static GuiSettings parseSettings(final YamlConfiguration cfg, final String guiKey) {

        final SoundData openSoundData = parseSoundData(cfg.getString("open-sound", "none"), guiKey, "open-sound");
        final SoundData closeSoundData = parseSoundData(cfg.getString("close-sound", "none"), guiKey, "close-sound");
        final int updateInterval = cfg.getInt("update-interval", -1);
        final boolean cache = cfg.getBoolean("cache", false);
        final boolean cachePerPlayer = cfg.getBoolean("cache-per-player", false);
        final List<String> defaultFlags = cfg.getStringList("default-flags");

        final GuiFill fill = parseFill(cfg.getConfigurationSection("fill"), guiKey);

        return new GuiSettings(
                openSoundData,
                closeSoundData,
                updateInterval,
                fill,
                cache,
                cachePerPlayer,
                defaultFlags
        );
    }

    private static SoundData parseSoundData(final String value, final String guiKey, final String soundType) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("none"))
            return null;

        final String[] data = value.split(":");
        final SoundData soundData = new SoundData();
        final int indexOffset = data[0].equalsIgnoreCase("minecraft") ? 1 : 0;

        try {
            final Sound sound = Sound.valueOf(data[0].toUpperCase());
            soundData.setSound(sound.getKey().toString());
        } catch (final IllegalArgumentException exception) {
            if (data.length > 1) {
                soundData.setSound(data[0] + ":" + data[1]);
            } else {
                soundData.setSound(data[0]);
            }
        }

        if (data.length > (1 + indexOffset)) {
            try {
                final float volume = Float.parseFloat(data[1 + indexOffset]);
                soundData.setVolume(volume);
            } catch (final NumberFormatException exception) {
                CrownCore.log.warn("[CrownGUI] Invalid sound volume value '" + data[1 + indexOffset] + "' for " + soundType + " in GUI " + guiKey);
            }
        }

        if (data.length > (2 + indexOffset)) {
            try {
                final float pitch = Float.parseFloat(data[2 + indexOffset]);
                soundData.setPitch(pitch);
            } catch (final NumberFormatException exception) {
                CrownCore.log.warn("[CrownGUI] Invalid sound pitch value '" + data[2 + indexOffset] + "' for " + soundType + " in GUI " + guiKey);
            }
        }

        return soundData;
    }

    private static GuiFill parseFill(final ConfigurationSection section, final String guiKey) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return new GuiFill(false, null);
        }

        final String materialStr = section.getString("material", "");
        final Material material = Material.matchMaterial(materialStr);

        if (material == null) {
            CrownCore.log.warn("[CrownGUI] Invalid fill material '" + materialStr + "' in GUI " + guiKey);
            return new GuiFill(false, null);
        }

        final ItemBuilder builder = new ItemBuilder(material)
                .name(section.getString("name", " "));

        return new GuiFill(true, builder);
    }

    private static void extractGuiResources(final Plugin plugin, final File targetFolder) {
        CrownCore.log.debug("[GuiLoader] Extracting GUI resources for plugin: " + plugin.getName() + " -> " + targetFolder.getAbsolutePath());
        try {
            // 1. Try JavaPlugin getFile() via reflection (Primary for Paper / Canvas / Spigot)
            final File pluginJar = getPluginJarFile(plugin);
            CrownCore.log.debug("[GuiLoader] getPluginJarFile returned: " + (pluginJar == null ? "null" : pluginJar.getAbsolutePath() + " (exists=" + pluginJar.exists() + ")"));

            if (pluginJar != null && pluginJar.exists()) {
                if (pluginJar.isDirectory()) {
                    final File guiSourceDir = new File(pluginJar, "gui");
                    CrownCore.log.debug("[GuiLoader] pluginJar is directory. guiSourceDir: " + guiSourceDir.getAbsolutePath() + " (exists=" + guiSourceDir.exists() + ")");
                    if (guiSourceDir.exists() && guiSourceDir.isDirectory()) {
                        copyDirectory(guiSourceDir, targetFolder, plugin);
                        return;
                    }
                } else if (pluginJar.isFile()) {
                    final boolean extracted = copyFolderFromJarFile(plugin, pluginJar, "gui", targetFolder);
                    CrownCore.log.debug("[GuiLoader] copyFolderFromJarFile (getPluginJarFile) returned: " + extracted);
                    if (extracted) {
                        return;
                    }
                }
            }

            // 2. Try ProtectionDomain CodeSource location (with URL decoding / jar:file: stripping)
            final CodeSource source = plugin.getClass().getProtectionDomain().getCodeSource();
            CrownCore.log.debug("[GuiLoader] CodeSource location: " + (source == null ? "null" : source.getLocation()));
            if (source != null && source.getLocation() != null) {
                final File sourceFile = urlToFile(source.getLocation());
                CrownCore.log.debug("[GuiLoader] urlToFile returned: " + (sourceFile == null ? "null" : sourceFile.getAbsolutePath() + " (exists=" + sourceFile.exists() + ")"));
                if (sourceFile != null && sourceFile.exists()) {
                    if (sourceFile.isDirectory()) {
                        final File guiSourceDir = new File(sourceFile, "gui");
                        CrownCore.log.debug("[GuiLoader] sourceFile is directory. guiSourceDir: " + guiSourceDir.getAbsolutePath() + " (exists=" + guiSourceDir.exists() + ")");
                        if (guiSourceDir.exists() && guiSourceDir.isDirectory()) {
                            copyDirectory(guiSourceDir, targetFolder, plugin);
                            return;
                        }
                    } else if (sourceFile.isFile()) {
                        final boolean extracted = copyFolderFromJarFile(plugin, sourceFile, "gui", targetFolder);
                        CrownCore.log.debug("[GuiLoader] copyFolderFromJarFile (CodeSource) returned: " + extracted);
                        if (extracted) {
                            return;
                        }
                    }
                }
            }

            // 3. Fallback via ClassLoader & JarURLConnection
            CrownCore.log.debug("[GuiLoader] Falling back to copyFolderFromClassLoader for " + plugin.getName());
            copyFolderFromClassLoader(plugin, "gui", targetFolder);

        } catch (final Exception ex) {
            CrownCore.log.warn("[GuiLoader] Failed to extract GUI resources for " + plugin.getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static File getPluginJarFile(final Plugin plugin) {
        try {
            if (plugin instanceof JavaPlugin) {
                final Method method = JavaPlugin.class.getDeclaredMethod("getFile");
                method.setAccessible(true);
                final File file = (File) method.invoke(plugin);
                CrownCore.log.debug("[GuiLoader] getFile() reflection result: " + (file == null ? "null" : file.getAbsolutePath()));
                return file;
            }
        } catch (final Exception ex) {
            CrownCore.log.debug("[GuiLoader] getFile() reflection failed: " + ex.getMessage());
        }
        return null;
    }

    private static File urlToFile(final URL url) {
        if (url == null) return null;
        try {
            String path = url.toExternalForm();
            if (path.startsWith("jar:")) {
                path = path.substring(4);
            }
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }
            final int bangIdx = path.indexOf("!");
            if (bangIdx != -1) {
                path = path.substring(0, bangIdx);
            }
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            final File file = new File(path);
            CrownCore.log.debug("[GuiLoader] urlToFile decoded path: " + path + " (exists=" + file.exists() + ")");
            if (file.exists()) {
                return file;
            }
        } catch (final Exception ex) {
            CrownCore.log.debug("[GuiLoader] urlToFile exception: " + ex.getMessage());
        }

        try {
            final File file = new File(url.toURI());
            CrownCore.log.debug("[GuiLoader] urlToFile URI result: " + file.getAbsolutePath() + " (exists=" + file.exists() + ")");
            return file;
        } catch (final Exception ignored) {}

        return null;
    }

    private static boolean copyFolderFromJarFile(
            final Plugin plugin,
            final File jarFileFile,
            final String jarPath,
            final File targetFolder
    ) {
        CrownCore.log.debug("[GuiLoader] copyFolderFromJarFile attempting to open JarFile: " + jarFileFile.getAbsolutePath());
        try (final JarFile jarFile = new JarFile(jarFileFile)) {
            return copyFolderFromJar(plugin, jarFile, jarPath, targetFolder);
        } catch (final Exception ex) {
            CrownCore.log.warn("[GuiLoader] copyFolderFromJarFile failed for " + jarFileFile.getAbsolutePath() + ": " + ex.getMessage());
            return false;
        }
    }

    private static boolean copyFolderFromJar(
            final Plugin plugin,
            final JarFile jarFile,
            final String jarPath,
            final File targetFolder
    ) throws IOException {

        final Enumeration<JarEntry> entries = jarFile.entries();
        final String prefix = jarPath.endsWith("/") ? jarPath : jarPath + "/";
        boolean extractedAny = false;
        int matchCount = 0;

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();

            if (!name.startsWith(prefix)) continue;

            matchCount++;
            final String relative = name.substring(prefix.length());
            if (relative.isEmpty()) continue;

            extractedAny = true;
            targetFolder.mkdirs();
            final File outFile = new File(targetFolder, relative);

            if (entry.isDirectory()) {
                outFile.mkdirs();
                continue;
            }

            if (outFile.exists()) {
                CrownCore.log.debug("[GuiLoader] GUI config already exists: " + relative + " for " + plugin.getName());
                continue;
            }

            outFile.getParentFile().mkdirs();

            CrownCore.log.info("Extracting new GUI config: " + relative + " for " + plugin.getName());

            try (final InputStream in = jarFile.getInputStream(entry);
                 final OutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            }
        }

        CrownCore.log.debug("[GuiLoader] Scanned JarFile " + jarFile.getName() + " -> found " + matchCount + " entries matching prefix '" + prefix + "' for " + plugin.getName());
        return extractedAny;
    }

    private static void copyDirectory(final File sourceDir, final File targetFolder, final Plugin plugin) throws IOException {
        final File[] files = sourceDir.listFiles();
        if (files == null) return;

        for (final File file : files) {
            final File targetFile = new File(targetFolder, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, targetFile, plugin);
            } else if (!targetFile.exists()) {
                targetFolder.mkdirs();
                CrownCore.log.info("Extracting new GUI config: " + file.getName() + " for " + plugin.getName());
                try (final InputStream in = new FileInputStream(file);
                     final OutputStream out = new FileOutputStream(targetFile)) {
                    in.transferTo(out);
                }
            }
        }
    }

    private static void copyFolderFromClassLoader(final Plugin plugin, final String jarPath, final File targetFolder) {
        try {
            URL url = plugin.getClass().getClassLoader().getResource(jarPath);
            CrownCore.log.debug("[GuiLoader] ClassLoader.getResource(" + jarPath + ") -> " + url);
            if (url == null) {
                url = plugin.getClass().getClassLoader().getResource(jarPath + "/");
                CrownCore.log.debug("[GuiLoader] ClassLoader.getResource(" + jarPath + "/) -> " + url);
            }
            if (url == null) {
                url = plugin.getClass().getResource("/" + jarPath);
                CrownCore.log.debug("[GuiLoader] Class.getResource(/" + jarPath + ") -> " + url);
            }
            if (url == null) return;

            if ("file".equals(url.getProtocol())) {
                final File file = new File(url.toURI());
                CrownCore.log.debug("[GuiLoader] ClassLoader URL is file: " + file.getAbsolutePath());
                if (file.isDirectory()) {
                    copyDirectory(file, targetFolder, plugin);
                }
            } else if ("jar".equals(url.getProtocol())) {
                CrownCore.log.debug("[GuiLoader] ClassLoader URL is jar: " + url);
                final java.net.URLConnection urlConnection = url.openConnection();
                if (urlConnection instanceof JarURLConnection) {
                    final JarURLConnection jarConnection = (JarURLConnection) urlConnection;
                    try (final JarFile jarFile = jarConnection.getJarFile()) {
                        copyFolderFromJar(plugin, jarFile, jarPath, targetFolder);
                    }
                }
            }
        } catch (final Exception ex) {
            CrownCore.log.warn("[GuiLoader] Exception in copyFolderFromClassLoader: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

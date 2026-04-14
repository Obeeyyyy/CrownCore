/* CrownPlugins - CrownCore */
/* 18.08.2024 - 00:20 */

package de.obey.crown.core.data.plugin;

import de.obey.crown.core.data.plugin.storage.PluginStorageConfig;
import de.obey.crown.core.data.plugin.storage.datakey.DataKeyRegistry;
import de.obey.crown.core.gui.GuiLoader;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.util.FileUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Getter
public class CrownConfig implements CrowPlugin {

    private final CrownCore crownCore = CrownCore.getInstance();
    @NonNull
    private final Plugin plugin;

    private Messanger messanger;
    private Sounds sounds;

    private File messageFile, configFile, soundFile;

    private PluginStorageConfig pluginStorageConfig;

    public CrownConfig(@NonNull Plugin plugin) {
        this.plugin = plugin;
        CrownPluginRegistry.register(plugin);

        createFiles();

        sounds = new Sounds(plugin);
        messanger = new Messanger(plugin, sounds);

        loadGuis();
        loadConfig();
        loadMessages();
        loadSounds();
    }

    @Override
    public void createFiles() {
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        messageFile = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
        configFile = FileUtil.getGeneratedFile(plugin, "config.yml", true);
        soundFile = FileUtil.getGeneratedFile(plugin, "sounds.yml", true);
    }

    @Override
    public void loadMessages() {
        messanger.load();
    }

    @Override
    public void loadSounds() {
        sounds.load();
    }

    public void loadGuis() {
        GuiLoader.loadAll(plugin);
    }

    public void reload() {
        createFiles();
        loadConfig();
        loadMessages();
        loadSounds();

        if(pluginStorageConfig != null)
            crownCore.getPluginStorageManager().shutdownPluginConnections(plugin);

        crownCore.getPluginStorageManager().createConnection(this).thenApply(success -> {
            if(success) {
                if(pluginStorageConfig != null) {
                    crownCore.getPluginStorageManager().createPluginDataTables(plugin.getName(), false);
                }

                if(DataKeyRegistry.pluginHasKeys(plugin))
                    crownCore.getPluginStorageManager().createPlayerDataTable(plugin.getName());
            }
            return success;
        });
    }

    public void loadConfig() {
        boolean changed;
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getConfigFile());
        configuration.options().parseComments(true);

        final YamlConfiguration defaults = new YamlConfiguration();
        defaults.options().parseComments(true);

        try (final InputStream stream = plugin.getResource("config.yml")) {
            if (stream != null) {
                defaults.load(new InputStreamReader(stream, StandardCharsets.UTF_8));

                changed = compareDefaults(defaults, configuration);
                changed |= removeDeprecatedKeys(defaults, configuration);

                if (changed) {
                    backupConfigFile();
                    FileUtil.saveConfigurationIntoFile(configuration, getConfigFile());
                }
            }
        } catch (final IOException | InvalidConfigurationException e) {
            CrownCore.log.warn("Failed to update config for plugin " + plugin.getName());
            e.printStackTrace();
        }

        if (configuration.contains("storage"))
            pluginStorageConfig = new PluginStorageConfig(this, configuration);

        if (DataKeyRegistry.pluginHasKeys(plugin))
            crownCore.getPluginStorageManager().registerPlayerDataPlugin(this);

    }

    private boolean compareDefaults(final YamlConfiguration defaults, final YamlConfiguration configuration) {
        CrownCore.log.debug("starting config checks for " + plugin.getName());
        boolean changed = false;

        configuration.options().setHeader(defaults.options().getHeader());

        final Set<String> keys = defaults.getKeys(false);

        if(keys.isEmpty())
            return false;

        for (final String key : keys) {
            final Object obj = defaults.get(key);

            if(obj instanceof ConfigurationSection) {
                configuration.setComments(key, defaults.getComments(key));
                configuration.setInlineComments(key, defaults.getInlineComments(key));
                changed |= processSection((ConfigurationSection) obj, configuration, key);
                continue;
            }

            if(!configuration.contains(key)) {
                changed = true;
                final Object val = defaults.get(key);
                configuration.set(key, val);
                configuration.setComments(key, defaults.getComments(key));
                configuration.setInlineComments(key, defaults.getInlineComments(key));;
                CrownCore.log.info("(" + plugin.getName() + ") generated missing key in config: " + key + " -> " + val);
            }

        }

        return changed;
    }

    private boolean processSection(final ConfigurationSection section, final YamlConfiguration configuration, final String path) {
        boolean changed = false;
        for (final String key : section.getKeys(false)) {
            final String fullPath = path == null || path.isEmpty()
                    ? key
                    : path + "." + key;

            final Object obj = section.get(key);

            if (obj instanceof ConfigurationSection nested) {
                configuration.setComments(fullPath, section.getComments(key));
                configuration.setInlineComments(fullPath, section.getInlineComments(key));
                changed |= processSection(nested, configuration, fullPath);
                continue;
            }

            if(!configuration.contains(fullPath)) {
                changed = true;
                final Object val = section.get(key);
                configuration.set(fullPath, val);
                configuration.setComments(fullPath, section.getComments(key));
                configuration.setInlineComments(fullPath, section.getInlineComments(key));
                CrownCore.log.info("(" + plugin.getName() + ") generated missing key in config: " + fullPath + " -> " + val);
            }
        }

        return changed;
    }

    private boolean removeDeprecatedKeys(final YamlConfiguration defaults, final YamlConfiguration configuration) {
        boolean changed = false;

        final Set<String> validKeys = defaults.getKeys(true);
        final Set<String> existingKeys = new HashSet<>(configuration.getKeys(true));

        for (String key : existingKeys) {
            if (!validKeys.contains(key)) {
                configuration.set(key, null);
                changed = true;

                CrownCore.log.info("(" + plugin.getName() + ") removed deprecated config key: " + key);
            }
        }

        return changed;
    }

    private void backupConfigFile() {
        final File original = getConfigFile();

        if (!original.exists())
            return;

        final File backupFolder = new File(original.getParentFile(), "config-backups");

        if (!backupFolder.exists())
            backupFolder.mkdirs();

        final String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        final File backupFile = new File(backupFolder, original.getName().replace(".yml", "") + "-" + timestamp + ".yml");

        try {
            Files.copy(original.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            CrownCore.log.info("(" + plugin.getName() + ") created config backup: " + backupFile.getName());

        } catch (IOException exception) {
            CrownCore.log.warn("(" + plugin.getName() + ") failed to create config backup");
            exception.printStackTrace();
        }
    }

    public void saveConfig() {}

    /***
     * Loads the storage part of the config.yml
     * @param configuration YamlConfiguration for config file
     */
    @Deprecated
    public void loadPluginStorageConfig(final YamlConfiguration configuration) {
        pluginStorageConfig = new PluginStorageConfig(this, configuration);
    }

    @Deprecated
    public void registerPluginWithPlayerData() {
        crownCore.getPluginStorageManager().registerPlayerDataPlugin(this);
    }
}

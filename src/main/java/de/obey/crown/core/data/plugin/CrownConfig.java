/* CrownPlugins - CrownCore */
/* 18.08.2024 - 00:20 */

package de.obey.crown.core.data.plugin;

import de.obey.crown.core.data.plugin.storage.PluginStorageConfig;
import de.obey.crown.core.gui.GuiLoader;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.util.FileUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

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
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

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

    public void reload() {}

    public void loadConfig() {}

    public void saveConfig() {}

    /***
     * Loads the storage part of the config.yml
     * @param configuration YamlConfiguration for config file
     */
    public void loadPluginStorageConfig(final YamlConfiguration configuration) {
        pluginStorageConfig = new PluginStorageConfig(this, configuration);
    }

    public void registerPluginWithPlayerData() {
        crownCore.getPluginStorageManager().registerPlayerDataPlugin(this);
    }
}

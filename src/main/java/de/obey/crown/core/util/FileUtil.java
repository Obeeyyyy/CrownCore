/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@UtilityClass
public final class FileUtil {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    public File getFile(final String path, final String fileName) {
        return new File(path + "/" + fileName);
    }

    public File getGeneratedFile(final Plugin plugin, final String fileName, final boolean generate) {
        final File file = new File(plugin.getDataFolder().getPath() + "/" + fileName);

        if (!file.exists() && generate) {
            try {
                plugin.saveResource(fileName, false);
            } catch (final IllegalArgumentException ignored) {
            }
        }

        return file;
    }

    public File getCreatedFile(final Plugin plugin, final String fileName, final boolean create) {
        final File file = new File(plugin.getDataFolder().getPath() + "/" + fileName);

        if (!file.exists() && create) {
            try {
                file.createNewFile();
            } catch (final IOException ignored) {
            }
        }

        return file;
    }

    public File getCoreFile(final String fileName) {
        return getGeneratedCoreFile(fileName, false);
    }

    public File getGeneratedCoreFile(final String fileName, final boolean generate) {
        final File file = new File(CrownCore.getInstance().getDataFolder() + "/" + fileName);

        if (!file.exists() && generate) {
            CrownCore.getInstance().saveResource(fileName, false);
        }

        return file;
    }

    public File getCreatedCoreFile(final String fileName, final boolean create) {
        final File file = new File(CrownCore.getInstance().getDataFolder() + "/" + fileName);

        if (!file.exists() && create) {
            try {
                file.createNewFile();
            } catch (final IOException ignored) {
            }
        }

        return file;
    }

    public void saveConfigurationIntoFile(final YamlConfiguration configuration, final File file) {
        if (configuration == null || file == null) {
            Bukkit.getLogger().warning("Could not save file.");
            return;
        }

        try {
            configuration.save(file);
        } catch (final IOException ignored) {
        }
    }

    public int getInt(final YamlConfiguration configuration, final String path, final int defaultValue) {
        if (configuration.contains(path))
            return configuration.getInt(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public long getLong(final YamlConfiguration configuration, final String path, final long defaultValue) {
        if (configuration.contains(path))
            return configuration.getLong(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public double getDouble(final YamlConfiguration configuration, final String path, final double defaultValue) {
        if (configuration.contains(path))
            return configuration.getDouble(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public String getString(final YamlConfiguration configuration, final String path, String defaultValue) {
        if (configuration.contains(path)) {
            return configuration.getString(path);
        }

        configuration.set(path, defaultValue);
        return defaultValue;
    }

    public boolean getBoolean(final YamlConfiguration configuration, final String path, final boolean defaultValue) {
        if (configuration.contains(path))
            return configuration.getBoolean(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public ArrayList<String> getStringArrayList(final YamlConfiguration configuration, final String path, final ArrayList defaultValue) {
        if (configuration.contains(path))
            return (ArrayList<String>) configuration.getList(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public ArrayList<Integer> getIntArrayList(final YamlConfiguration configuration, final String path, final ArrayList defaultValue) {
        if (configuration.contains(path))
            return (ArrayList<Integer>) configuration.getList(path);

        configuration.set(path, defaultValue);

        return defaultValue;
    }

    public ArrayList<ItemStack> getItemStackList(final YamlConfiguration configuration, final String path, final ArrayList defaultValue) {
        if (!configuration.contains((path)))
            return defaultValue;

        /*

        test:
          path:
            1: item here
            2: item here

         */

        final ArrayList<ItemStack> items = new ArrayList<>();

        if (!configuration.contains(path))
            return items;

        if (configuration.getConfigurationSection(path).getKeys(false).isEmpty())
            return items;

        for (final String key : configuration.getConfigurationSection(path).getKeys(false)) {
            items.add(configuration.getItemStack(path + "." + key));
        }

        return items;
    }

    public void setItemStackList(final YamlConfiguration configuration, final String path, final ArrayList<ItemStack> items) {
        if (items.isEmpty())
            return;

        int slot = 0;
        for (final ItemStack item : items) {
            configuration.set(path + "." + slot, item);
            slot++;
        }
    }

    public ItemStack getItemStack(final YamlConfiguration configuration, final String path, final ItemStack defaultValue) {
        if (configuration.contains(path))
            return configuration.getItemStack(path);

        configuration.set(path, defaultValue);
        return defaultValue;
    }

    public Material getMaterial(final YamlConfiguration configuration, final String path, final Material defaultValue) {
        if (configuration.contains(path)) {
            try {
                return Material.valueOf(configuration.getString(path));
            } catch (IllegalArgumentException ignored) {

            }
        }

        configuration.set(path, defaultValue.name());
        return defaultValue;
    }
}

/* CrownPlugins - CrownCore */
/* 11.02.2025 - 02:28 */

package de.obey.crown.core.noobf;

import com.google.common.collect.Maps;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.PlaceholderUtil;
import de.obey.crown.core.util.TextUtil;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Set;

public final class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "cc";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Obey";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    public Placeholders() {
        loadPlaceholders();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        final String[] args = params.split("_");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("prefix"))
                return TextUtil.translateCorePlaceholder("%prefix%");

            if (args[0].equalsIgnoreCase("accent"))
                return TextUtil.translateCorePlaceholder("%accent%");

            if (args[0].equalsIgnoreCase("white"))
                return TextUtil.translateCorePlaceholder("%white%");
        }

        if(placeholders.containsKey(params))
            return TextUtil.translateColors(placeholders.get(params));

        return "&cinvalid placeholder";
    }

    private final Map<String, String> placeholders = Maps.newConcurrentMap();

    public void loadPlaceholders() {
        if(!PlaceholderUtil.papiEnabled)
            return;

        placeholders.clear();

        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "placeholders.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if(!configuration.contains("placeholders"))
            return;

        final Set<String> keys = configuration.getConfigurationSection("placeholders").getKeys(false);
        
        if(keys.isEmpty())
            return;

        for (final String key : keys) {
            placeholders.put(key, FileUtil.getString(configuration, "placeholders." + key, ""));
        }
    }
}

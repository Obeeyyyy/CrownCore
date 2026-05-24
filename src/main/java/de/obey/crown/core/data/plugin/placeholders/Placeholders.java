/* CrownPlugins - CrownCore */
/* 11.02.2025 - 02:28 */

package de.obey.crown.core.data.plugin.placeholders;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.noobf.PluginConfig;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.PlaceholderUtil;
import de.obey.crown.core.util.TextUtil;
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

    private final PluginConfig pluginConfig;

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

    public Placeholders(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        loadPlaceholders();
        loadConditionalPlaceholders();
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

            if(args[0].equalsIgnoreCase("playtime")) {
                try {
                    final long seconds = Long.parseLong(PlaceholderAPI.setPlaceholders(player, "%statistic_seconds_played%"));
                    return TextUtil.formatTimeStringWithFormat(seconds*1000, pluginConfig.getPlaytimeTimeFormat());
                } catch (final NumberFormatException exception) {
                    return "0";
                }
            }
        }

        if(placeholders.containsKey(params))
            return PlaceholderAPI.setPlaceholders(player, placeholders.get(params));

        if(conditionalPlaceholders.containsKey(params))
            return conditionalPlaceholders.get(params).evaluate(player);

        return "&cinvalid placeholder";
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String params) {

        if(placeholders.containsKey(params))
            return PlaceholderAPI.setPlaceholders(player, placeholders.get(params));

        if(conditionalPlaceholders.containsKey(params))
            return conditionalPlaceholders.get(params).evaluate(player);

        return "&cinvalid placeholder";
    }

    private final Map<String, String> placeholders = Maps.newConcurrentMap();
    private final Map<String, ConditionalPlaceholder> conditionalPlaceholders = Maps.newConcurrentMap();

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
            placeholders.put(key, FileUtil.getRawString(configuration, "placeholders." + key, ""));
        }
    }

    public void loadConditionalPlaceholders() {
        if(!PlaceholderUtil.papiEnabled)
            return;

        conditionalPlaceholders.clear();

        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "placeholders.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if(!configuration.contains("conditional-placeholders"))
            return;

        final Set<String> keys = configuration.getConfigurationSection("conditional-placeholders").getKeys(false);

        if(keys.isEmpty())
            return;

        for (final String key : keys) {
            final ConditionalPlaceholder conditionalPlaceholder = new ConditionalPlaceholder(key, configuration.getConfigurationSection("conditional-placeholders." + key));
            conditionalPlaceholders.put(key, conditionalPlaceholder);
        }

    }
}

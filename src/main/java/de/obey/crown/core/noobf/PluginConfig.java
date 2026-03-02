/* CrownPlugins - CrownCore */
/* 18.08.2024 - 01:22 */

package de.obey.crown.core.noobf;

import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.data.plugin.TeleportMessageType;
import de.obey.crown.core.data.redis.RedisConfiguration;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.TextUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.LocaleUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public final class PluginConfig extends CrownConfig {

    private TeleportMessageType teleportMessageType;

    private int teleportDelay, messageDelay, commandDelay, dataCacheTime;
    private boolean instantTeleport = false, instantRespawn = true, teleportOnJoin, updateReminder = true, teleportParticles = true, useShortFormat = false;
    private List<String> instantTeleportWorlds;
    private List<String> instantTeleportRegions;
    private String defaultTimeFormat, teleportationTimeFormat, bedrockPrefix;

    private RedisConfiguration redisConfiguration;

    public PluginConfig(@NonNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        final File file = FileUtil.getFile(this.getPlugin().getDataFolder().getPath(), "config.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        setDataCacheTime(FileUtil.getInt(configuration, "data-cache-time", 3600000));
        setTeleportMessageType(FileUtil.getString(configuration, "teleport.message-type", "actionbar").equalsIgnoreCase("actionbar") ? TeleportMessageType.ACTIONBAR : TeleportMessageType.BOSSBAR);
        setInstantTeleport(FileUtil.getBoolean(configuration, "instant-teleport.always", false));
        setTeleportDelay(FileUtil.getInt(configuration, "teleport.delay", 10));
        setTeleportParticles(FileUtil.getBoolean(configuration, "teleport.particles", true));
        setInstantTeleportWorlds(FileUtil.getStringArrayList(configuration, "instant-teleport.worlds", new ArrayList<>()));
        setInstantTeleportRegions(FileUtil.getStringArrayList(configuration, "instant-teleport.regions", new ArrayList<>()));
        setMessageDelay(FileUtil.getInt(configuration, "cooldown.message", 0));
        setCommandDelay(FileUtil.getInt(configuration, "cooldown.command", 0));
        setUpdateReminder(FileUtil.getBoolean(configuration, "update-reminder", true));

        CrownCore.log.setDebug(FileUtil.getBoolean(configuration, "debug-mode", false));

        Locale locale = LocaleUtils.toLocale(FileUtil.getString(configuration, "number-format.locale", "en_US"));
        if(locale == null) {
            locale = Locale.ENGLISH;
        }
        TextUtil.setDecimalFormat(new DecimalFormat(FileUtil.getString(configuration, "number-format.default-format", "#,###.##"), new DecimalFormatSymbols(locale)));
        TextUtil.setUseShortFormat(FileUtil.getBoolean(configuration, "number-format.use-short-format", false));

        setDefaultTimeFormat(FileUtil.getString(configuration, "time-formats.default", "%hh%:%mm%:%ss%"));
        setTeleportationTimeFormat(FileUtil.getString(configuration, "time-formats.teleportation", "%ss%.%t%s"));

        loadRedisConfiguration(configuration);

        FileUtil.saveConfigurationIntoFile(configuration, file);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getConfigFile());

        configuration.set("debug-mode", CrownCore.log.isDebug());

        FileUtil.saveConfigurationIntoFile(configuration, getConfigFile());
    }

    private void loadRedisConfiguration(final YamlConfiguration configuration) {

        ConfigurationSection section;

        if(configuration.contains("redis")) {
            section = configuration.getConfigurationSection("redis");
        } else {
            section = configuration.createSection("redis");
        }

        if (redisConfiguration == null)
            redisConfiguration = new RedisConfiguration();

        redisConfiguration.setEnabled(FileUtil.getBoolean(section, "enabled", false));
        redisConfiguration.setHost(FileUtil.getString(section, "host", "127.0.0.1"));
        redisConfiguration.setPort(FileUtil.getInt(section, "port", 6379));
        redisConfiguration.setUsername(FileUtil.getString(section, "username", ""));
        redisConfiguration.setPassword(FileUtil.getString(section, "password", ""));

        redisConfiguration.setDatabase(FileUtil.getInt(section, "database", 0));
        redisConfiguration.setSsl(FileUtil.getBoolean(section, "ssl", false));
        redisConfiguration.setTimeout(FileUtil.getInt(section, "timeout", 5000));

        configuration.set("redis", section);
    }
}

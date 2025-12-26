package de.obey.crown.core.data.plugin.storage;

import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter @Setter
public class PluginStorageConfig {

    private StorageType storageType;
    private String host, database, username, password, characterEncoding = "UTF-8";
    private int maxPoolSize, minIdle, maxLifetime, keepAliveTime, maxReconnects = 10, connectTimeout = 5000;

    private boolean autoReconnect = true, useUnicode = true, holdResultsOpenOverStatementClose = true, dontTrackOpenResources = true;

    public PluginStorageConfig(final CrownConfig pluginConfig, final YamlConfiguration configuration) {
        CrownCore.log.debug("loading storage config for plugin: " + pluginConfig.getPlugin().getName());
        try {
            storageType = StorageType.valueOf(FileUtil.getString(configuration, "storage.method", "yml").toUpperCase());
        } catch (IllegalArgumentException exception) {
            storageType = StorageType.H2;
            CrownCore.log.warn("invalid storage.method for " + pluginConfig.getPlugin().getName() + " in config.yml");
        }

        setHost(FileUtil.getString(configuration, "storage.data.host", "crownhost"));
        setDatabase(FileUtil.getString(configuration, "storage.data.database", "crowndatabase"));
        setUsername(FileUtil.getString(configuration, "storage.data.username", "crownuser"));
        setPassword(FileUtil.getString(configuration, "storage.data.password", "crownpassword"));

        setMaxPoolSize(FileUtil.getInt(configuration, "storage.data.pool-settings.maximum-pool-size", 10));
        setMinIdle(FileUtil.getInt(configuration, "storage.data.pool-settings.minimum-idle", 10));
        setMaxLifetime(FileUtil.getInt(configuration, "storage.data.pool-settings.maximum-lifetime", 1800000));
        setKeepAliveTime(FileUtil.getInt(configuration, "storage.data.pool-settings.keepalive-time", 0));
        setConnectTimeout(FileUtil.getInt(configuration, "storage.data.pool-settings.connection-timeout", 5000));

        CrownCore.log.debug(" - storage method: " + storageType.name());
        CrownCore.log.debug(" - host: " + host);
        CrownCore.log.debug(" - user: " + username);
        CrownCore.log.debug(" - password: " + password.substring(0, password.length() / 2) + "*");
    }
}

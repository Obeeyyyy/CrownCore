package de.obey.crown.core.data.plugin.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.obey.crown.core.data.plugin.storage.datakey.DataKey;
import de.obey.crown.core.data.plugin.storage.datakey.DataKeyRegistry;
import de.obey.crown.core.data.plugin.storage.player.PlayerData;
import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.data.plugin.storage.player.PlayerDataSchema;
import de.obey.crown.core.data.plugin.storage.plugin.PluginDataSchema;
import de.obey.crown.core.noobf.CrownCore;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class PluginStorageManager {

    private final ExecutorService executor;

    private final Map<String, HikariDataSource> connections = new ConcurrentHashMap<>();
    private final Map<String, PlayerDataSchema> playerDataSchemas = new ConcurrentHashMap<>();
    private final Map<String, List<PluginDataSchema>> pluginDataSchemas = new ConcurrentHashMap<>();

    /***
     * Initiates the database connection (h2/mysql/mariadb) with the settings read out of the config.yml
     * @param pluginConfig the CrownConfig instance to read settings from
     */
    private void createConnection(final CrownConfig pluginConfig) {
        final PluginStorageConfig storageConfig = pluginConfig.getPluginStorageConfig();
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();

        executor.submit(() -> {
            if (connections.containsKey(pluginName)) {
                String driverClassName = "";
                try {
                    driverClassName = connections.get(pluginName).getConnection().getMetaData().getDriverName();
                    CrownCore.log.debug("plugin db connection already exists");
                    CrownCore.log.debug(" - driver: " + driverClassName);
                    CrownCore.log.debug(" - current storage method: " + storageConfig.getStorageType().name());
                } catch (final SQLException ignored) {}

                if (driverClassName.contains(storageConfig.getStorageType().name())) {
                    return;
                }
            }

            CrownCore.log.debug(" - creating new connection");

            final HikariConfig hikariConfig = new HikariConfig();

            String jdbcUrl;
            final Path h2DataFile = pluginConfig.getPlugin().getDataFolder().toPath().resolve(pluginName.toLowerCase());

            try {
                Class.forName("org.h2.Driver");
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            hikariConfig.setUsername("sa");
            hikariConfig.setPassword("");

            switch (storageConfig.getStorageType()) {
                case MYSQL, MARIADB -> {
                    jdbcUrl = "jdbc:" + (storageConfig.getStorageType().name().toLowerCase()) + "://" + storageConfig.getHost() + "/" + storageConfig.getDatabase() +
                            "?autoReconnect=" + storageConfig.isAutoReconnect() +
                            "&useUnicode=" + storageConfig.isUseUnicode() +
                            "&characterEncoding=" + storageConfig.getCharacterEncoding() +
                            "&dontTrackOpenResources=" + storageConfig.isDontTrackOpenResources() +
                            "&holdResultsOpenOverStatementClose=" + storageConfig.isHoldResultsOpenOverStatementClose();

                    hikariConfig.setUsername(storageConfig.getUsername());
                    hikariConfig.setPassword(storageConfig.getPassword());
                }

                case H2 -> {
                    jdbcUrl = "jdbc:h2:" + h2DataFile.toAbsolutePath();
                }

                default -> {
                    CrownCore.log.warn("invalid storage.method for " + pluginName + " in config.yml");
                    jdbcUrl = "jdbc:h2:" + h2DataFile.toAbsolutePath();
                }
            }

            CrownCore.log.debug("storage method for " + pluginName + ": " + storageConfig.getStorageType().name());
            CrownCore.log.debug(" - " + jdbcUrl);

            hikariConfig.setJdbcUrl(jdbcUrl);

            hikariConfig.setMaximumPoolSize(storageConfig.getMaxPoolSize());
            hikariConfig.setMaxLifetime(storageConfig.getMaxLifetime());
            hikariConfig.setMinimumIdle(storageConfig.getMinIdle());
            hikariConfig.setKeepaliveTime(storageConfig.getKeepAliveTime());

            hikariConfig.setPoolName("obey-" + pluginName);
            connections.put(pluginName, new HikariDataSource(hikariConfig));

            CrownCore.log.debug("created connection pool - " + hikariConfig.getPoolName());
        });
    }

    public void loadPluginDataPlugins() {
        createPluginDataTables();
    }

    /***
     * Registers a plugins data schema. Will read the schemas passed as params
     * @param pluginConfig he CrownConfig instance to read settings from
     * @param pluginDataSchema data schema passed
     */
    public void registerPluginDataPlugin(final CrownConfig pluginConfig, final PluginDataSchema pluginDataSchema) {
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();
        final List<PluginDataSchema> schemas = pluginDataSchemas.containsKey(pluginName) ? pluginDataSchemas.get(pluginName) : new ArrayList<>();

        if(!schemas.contains(pluginDataSchema)) {
            schemas.add(pluginDataSchema);
        }

        createConnection(pluginConfig);

        pluginDataSchemas.put(pluginName, schemas);
    }

    /***
     * Registers a plugin as a plugin using playerdata. Will read registered data keys and generate a playerdata table schema.
     * @param pluginConfig he CrownConfig instance to read settings from
     */
    public void registerPlayerDataPlugin(final CrownConfig pluginConfig) {
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();

        if(!DataKeyRegistry.pluginHasKeys(pluginName)) {
            return;
        }

        createConnection(pluginConfig);

        final PlayerDataSchema schema = new PlayerDataSchema(pluginName);
        playerDataSchemas.put(pluginName, schema);

        createPlayerDataTable(pluginName);
    }

    /***
     * creates the plugin data tables using the registered schemas
     */
    private void createPluginDataTables() {
        pluginDataSchemas.forEach((pluginName, schemas) -> {
            if(!connections.containsKey(pluginName)) {
                return;
            }

            CrownCore.log.debug(" > creating plugin data tables for " + pluginName);
            for (final PluginDataSchema pluginDataSchema : schemas) {
                CrownCore.log.debug(" - creating table: " + pluginDataSchema.getTableName());
                CrownCore.log.debug("   - primary key: " + pluginDataSchema.getPrimaryKeyName());

                executor.submit(() -> {
                    final StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                    stringBuilder.append(pluginDataSchema.getTableName()).append(" (");

                    for (final DataKey<?> key : pluginDataSchema.getDataKeys()) {
                        CrownCore.log.debug("   - data key: " + key.getName());
                        stringBuilder.append(key.getName()).append(" ").append(key.getSqlDataType());

                        if (key.getName().equalsIgnoreCase(pluginDataSchema.getPrimaryKeyName())) {
                            stringBuilder.append(" PRIMARY KEY");
                        }

                        stringBuilder.append(", ");
                    }

                    stringBuilder.setLength(stringBuilder.length() - 2);
                    stringBuilder.append(");");

                    try (final Connection conn = getConnectionForPluginName(pluginName);
                         final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(stringBuilder.toString());
                    } catch (final SQLException exception) {
                        CrownCore.log.warn("error creating plugin data tables: ");
                        CrownCore.log.warn(" - plugin: " + pluginName);
                        CrownCore.log.warn(" - table: " + pluginDataSchema.getTableName());
                        CrownCore.log.warn(" - exception: " + exception.getMessage());
                    }
                });
            }
        });
    }

    /***
     * creates the player data table using the generated schema
     * @param pluginName name of plugin the schema was generated for
     */
    private void createPlayerDataTable(final String pluginName) {
        executor.submit(() -> {
            CrownCore.log.debug("creating playerdata table for " + pluginName);
            final StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

            stringBuilder.append(pluginName).append(" (");
            stringBuilder.append("player_uuid CHAR(36) PRIMARY KEY, ");

            for (final DataKey<?> key : playerDataSchemas.get(pluginName).getDataKeys()) {
                stringBuilder.append(key.getName()).append(" ").append(key.getSqlDataType()).append(", ");
            }

            stringBuilder.setLength(stringBuilder.length() - 2);
            stringBuilder.append(");");

            try (final Connection conn = getConnectionForPluginName(pluginName);
                 final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(stringBuilder.toString());
            } catch (final SQLException exception) {
                CrownCore.log.warn("error creating player data table: ");
                CrownCore.log.warn(" - plugin: " + pluginName);
                CrownCore.log.warn(" - exception: " + exception.getMessage());
            }
        });
    }

    public Connection getConnectionForPluginName(final String name) throws SQLException {
        return connections.get(name).getConnection();
    }

    public void shutdownConnections() {
        for (final HikariDataSource hikariDataSource : connections.values()) {
            try {
                hikariDataSource.close();
            } catch (Exception ignored) {}
        }
        connections.clear();
    }

    private void insertDefaultPlayerDataValues(final UUID uuid, final PlayerDataSchema schema) throws SQLException {
        CrownCore.log.debug("inserting default values for " + uuid.toString());
        CrownCore.log.debug(" plugin: " + schema.getPluginName());

        final String pluginName = schema.getPluginName();
        final StringBuilder keys = new StringBuilder("player_uuid");
        final StringBuilder values = new StringBuilder("?");

        for (final DataKey<?> key : schema.getDataKeys()) {
            keys.append(", ").append(key.getName());
            values.append(", ?");
        }

        final String sql = "INSERT INTO " + pluginName + " (" + keys + ") VALUES (" + values + ")";

        try (final Connection conn = getConnectionForPluginName(pluginName);
             final PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            int index = 2;
            for (final DataKey<?> key : schema.getDataKeys()) {
                stmt.setObject(index++, key.getDefaultValue());
            }

            stmt.executeUpdate();
        } catch (final SQLException exception) {
            CrownCore.log.warn("error inserting default values: ");
            CrownCore.log.warn(" - plugin: " + pluginName);
            CrownCore.log.warn(" - exception: " + exception.getMessage());
        }
    }

    /***
     * loads playerdata from storage - only use in async calls
     * @param playerData instance to load data for
     * @return playerdata instance
     */
    public PlayerData loadPlayerData(final PlayerData playerData) {
        for (String pluginName : DataKeyRegistry.getRegistry().keySet()) {
            pluginName = pluginName.toLowerCase();
            CrownCore.log.debug("loading player data for plugin: " + pluginName);
            final PlayerDataSchema schema = playerDataSchemas.get(pluginName);
            String query = "SELECT * FROM " + pluginName + " WHERE player_uuid = ?";

            try (final Connection connection = getConnectionForPluginName(pluginName);
                 final PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, playerData.getUuid().toString());

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        for (final DataKey<?> key : schema.getDataKeys()) {
                            final Object value = resultSet.getObject(key.getName());

                            if (value != null) {
                                playerData.getData().put(key, value);
                            }
                        }
                    } else {
                        insertDefaultPlayerDataValues(playerData.getUuid(), schema);
                        schema.getDataKeys().forEach(key -> playerData.getData().put(key, key.getDefaultValue()));
                    }
                }
            } catch (final SQLException exception) {
                CrownCore.log.warn("error loading player data: ");
                CrownCore.log.warn(" - plugin: " + pluginName);
                CrownCore.log.warn(" - exception: " + exception.getMessage());
            }
        }

        return playerData;
    }

    /***
     * saved playerdata into storage - only use in async calls
     * @param playerData instance to save data for
     * @return playerdata instance
     */
    public PlayerData savePlayerData(final PlayerData playerData) {
        for (final String pluginName : DataKeyRegistry.getRegistry().keySet()) {
            final PlayerDataSchema schema = playerDataSchemas.get(pluginName);
            final StringBuilder query = new StringBuilder("UPDATE ").append(pluginName.toLowerCase()).append(" SET ");

            for (DataKey<?> key : schema.getDataKeys()) {
                query.append(key.getName()).append(" = ?, ");
            }

            query.setLength(query.length() - 2);
            query.append(" WHERE player_uuid = ?");

            try (final Connection connection = getConnectionForPluginName(pluginName);
                 final PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

                int index = 1;
                for (final DataKey<?> key : schema.getDataKeys()) {
                    preparedStatement.setObject(index++, playerData.getData().getOrDefault(key, key.getDefaultValue()));
                }

                preparedStatement.setString(index, playerData.getUuid().toString());
                preparedStatement.executeUpdate();
            } catch (final SQLException exception) {
                CrownCore.log.warn("error saving player data: ");
                CrownCore.log.warn(" - plugin: " + pluginName);
                CrownCore.log.warn(" - exception: " + exception.getMessage());
            }
        }

        return playerData;
    }
}

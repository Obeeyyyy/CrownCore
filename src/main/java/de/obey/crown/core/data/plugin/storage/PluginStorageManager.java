package de.obey.crown.core.data.plugin.storage;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.obey.crown.core.data.plugin.storage.datakey.DataKey;
import de.obey.crown.core.data.plugin.storage.datakey.DataKeyRegistry;
import de.obey.crown.core.data.plugin.storage.player.PlayerData;
import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.data.plugin.storage.player.PlayerDataSchema;
import de.obey.crown.core.data.plugin.storage.plugin.PluginDataSchema;
import de.obey.crown.core.event.PlayerDataLoadEvent;
import de.obey.crown.core.event.PlayerDataSaveEvent;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class PluginStorageManager {

    private final ExecutorService executor;

    private final Map<String, HikariDataSource> connections = new ConcurrentHashMap<>();
    private final Map<String, PlayerDataSchema> playerDataSchemas = new ConcurrentHashMap<>();
    private final Map<String, List<PluginDataSchema>> pluginDataSchemas = new ConcurrentHashMap<>();
    private final Map<String, CrownConfig> pluginConfigs = new ConcurrentHashMap<>();

    @Getter
    private final List<Plugin> playerSessionPlugins = new ArrayList<>();

    static {
        try {
            Class.forName("org.h2.Driver");
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Drivers might not be available in all environments, which is fine if not used
        }
    }

    /***
     * Initiates the database connection (h2/mysql/mariadb) with the settings read out of the config.yml
     * @param pluginConfig the CrownConfig instance to read settings from
     */
    public CompletableFuture<Boolean> createConnection(final CrownConfig pluginConfig) {
        final PluginStorageConfig storageConfig = pluginConfig.getPluginStorageConfig();

        if(storageConfig == null)
            return CompletableFuture.completedFuture(false);

        final String rawPluginName = pluginConfig.getPlugin().getName().toLowerCase();
        if (!rawPluginName.matches("[a-z0-9_]+")) {
            CrownCore.log.warn("Invalid plugin name for storage: " + rawPluginName);
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {

            if (connections.containsKey(rawPluginName)) {
                String driverClassName = "";
                try (final Connection connection = connections.get(rawPluginName).getConnection()) {
                    driverClassName = connection.getMetaData().getDriverName();
                    CrownCore.log.debug("plugin db connection already exists");
                    CrownCore.log.debug(" - driver: " + driverClassName);
                    CrownCore.log.debug(" - current storage method: " + storageConfig.getStorageType().name());
                } catch (final SQLException e) {
                    CrownCore.log.warn("error while creating connection: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }

                if (driverClassName.contains(storageConfig.getStorageType().name()))
                    return true;

                final HikariDataSource old = connections.remove(rawPluginName);
                if (old != null)
                    old.close();
            }

            CrownCore.log.debug(" - creating new connection");

            final HikariConfig hikariConfig = new HikariConfig();

            String jdbcUrl;
            final Path h2DataFile = pluginConfig.getPlugin().getDataFolder().toPath().resolve(rawPluginName);

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
                    CrownCore.log.warn("invalid storage.method for " + rawPluginName + " in config.yml");
                    jdbcUrl = "jdbc:h2:" + h2DataFile.toAbsolutePath();
                }
            }

            CrownCore.log.debug("storage method for " + rawPluginName + ": " + storageConfig.getStorageType().name());
            CrownCore.log.debug(" - " + jdbcUrl);

            hikariConfig.setJdbcUrl(jdbcUrl);

            hikariConfig.setMaximumPoolSize(storageConfig.getMaxPoolSize());
            hikariConfig.setMaxLifetime(storageConfig.getMaxLifetime());
            hikariConfig.setMinimumIdle(storageConfig.getMinIdle());
            hikariConfig.setKeepaliveTime(storageConfig.getKeepAliveTime());

            hikariConfig.setLeakDetectionThreshold(5000);
            hikariConfig.setInitializationFailTimeout(5000);
            hikariConfig.setConnectionTimeout(5000);
            hikariConfig.setValidationTimeout(5000);

            hikariConfig.setPoolName("obey-" + rawPluginName);
            connections.put(rawPluginName, new HikariDataSource(hikariConfig));

            CrownCore.log.debug("created connection pool - " + hikariConfig.getPoolName());

            return true;
        }, executor);
    }

    public void loadPluginDataPlugins() {
        createPluginDataTables();
    }

    public void loadPlayerDataPlugins() {
        for (final String pluginName : playerDataSchemas.keySet()) {
            createPlayerDataTable(pluginName.toLowerCase());
        }
    }

    /***
     * Registers a plugins data schema. Will read the schemas passed as params
     * @param pluginConfig the CrownConfig instance to read settings from
     * @param pluginDataSchema data schema passed
     */
    public void registerPluginDataPlugin(final CrownConfig pluginConfig, final PluginDataSchema pluginDataSchema) {
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();
        final List<PluginDataSchema> schemas = pluginDataSchemas.containsKey(pluginName) ? pluginDataSchemas.get(pluginName) : new ArrayList<>();

        if(!schemas.contains(pluginDataSchema))
            schemas.add(pluginDataSchema);

        if(!pluginConfigs.containsKey(pluginName)) {
            pluginConfigs.put(pluginName, pluginConfig);
            createConnection(pluginConfig);
        }

        pluginDataSchemas.put(pluginName, schemas);
    }

    /***
     * Registers a plugin as a plugin using playerdata. Will read registered data keys and generate a playerdata table schema.
     * @param pluginConfig the CrownConfig instance to read settings from
     */
    public void registerPlayerDataPlugin(final CrownConfig pluginConfig) {
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();

        CrownCore.log.debug("registering playerdata plugin: " + pluginName);

        if(!DataKeyRegistry.pluginHasKeys(pluginName)) {
            CrownCore.log.debug(" -> no datakey found, abort");
            return;
        }

        if(!pluginConfigs.containsKey(pluginName)) {
            pluginConfigs.put(pluginName, pluginConfig);
            createConnection(pluginConfig);
            CrownCore.log.debug(" -> created new connection");
        }

        final PlayerDataSchema schema = new PlayerDataSchema(pluginName);
        playerDataSchemas.put(pluginName, schema);
        CrownCore.log.debug(" -> created schema");
    }

    /***
     * Registers a plugin as a plugin using player sessions.
     * @param pluginConfig the CrownConfig instance to read settings from
     */
    public void registerPlayerSessionPlugin(final CrownConfig pluginConfig) {
        final String pluginName = pluginConfig.getPlugin().getName().toLowerCase();

        CrownCore.log.debug("registering player session plugin: " + pluginName);

        if(!pluginConfigs.containsKey(pluginName)) {
            pluginConfigs.put(pluginName, pluginConfig);
            createConnection(pluginConfig);
            CrownCore.log.debug(" -> created new connection");
        }
    }


    /***
     * creates the plugin data tables using the registered schemas
     */
    private void createPluginDataTables() {
        CrownCore.log.debug("creating all plugin data tables");
        pluginDataSchemas.forEach((pluginName, schemas) -> {
            CrownCore.log.debug(" - creating for: " + pluginName);
            createPluginDataTables(pluginName.toLowerCase(), false);
        });
    }

    public void createPluginDataTables(String pluginNamePlain, final boolean forceNewConnection) {
        final String pluginName = pluginNamePlain.toLowerCase();

        if(forceNewConnection) {
            createConnection(pluginConfigs.get(pluginName)).thenAccept((state) -> {
                if(state) {
                    executeTableCreation(pluginName);
                } else {
                    CrownCore.log.warn("could not execute table creation. no connection.");
                }
            });
        } else {
            if(!connections.containsKey(pluginName)) {
                if(!pluginConfigs.containsKey(pluginName))
                    return;

                createConnection(pluginConfigs.get(pluginName)).thenAccept((state) -> {
                    if(state) {
                        executeTableCreation(pluginName);
                    } else {
                        CrownCore.log.warn("could not execute table creation. no connection.");
                    }
                });
            } else {
                executeTableCreation(pluginName);
            }
        }
    }

    private void executeTableCreation(final String pluginName) {
        CrownCore.log.debug(" > creating plugin data tables for " + pluginName);

        final List<PluginDataSchema> schemas = pluginDataSchemas.get(pluginName);
        for (final PluginDataSchema pluginDataSchema : schemas) {
            CrownCore.log.debug(" - creating table: " + pluginDataSchema.getTableName());

            executor.execute(() -> {
                final StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                stringBuilder.append(pluginDataSchema.getTableName()).append(" (");

                final List<String> primaryKeys = new ArrayList<>();

                for (final Iterator<DataKey<?>> iterator = pluginDataSchema.getDataKeys().iterator(); iterator.hasNext(); ) {
                    final DataKey<?> key = iterator.next();

                    CrownCore.log.debug("   - data key: " + key.getName());

                    final StringBuilder column = new StringBuilder(key.getName())
                            .append(" ")
                            .append(key.getSqlDataType());

                    if (key.isNotNull()) column.append(" NOT NULL");
                    if (key.getDefaultValue() != null) {
                        if (Number.class.isAssignableFrom(key.getDataType()) || key.getDataType() == Boolean.class) {
                            column.append(" DEFAULT ").append(key.getDefaultValue());
                        } else {
                            column.append(" DEFAULT '").append(key.getDefaultValue()).append("'");
                        }
                    }
                    if (key.isAutoIncrement()) column.append(" AUTO_INCREMENT");
                    if (key.isUnique()) column.append(" UNIQUE");

                    stringBuilder.append(column);

                    if (iterator.hasNext()) {
                        stringBuilder.append(", ");
                    }

                    if (key.isPrimaryKey()) {
                        primaryKeys.add(key.getName());
                    }
                }

                if (!primaryKeys.isEmpty()) {
                    stringBuilder.append(", PRIMARY KEY (")
                            .append(String.join(", ", primaryKeys))
                            .append(")");
                }

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
    }

    /***
     * creates the base player data table and checks/adds the columns using the generated schema
     * @param rawPluginName name of plugin the schema was generated for
     */
    public void createPlayerDataTable(final String rawPluginName) {
        final String pluginName = rawPluginName.toLowerCase();

        executor.execute(() -> {
            CrownCore.log.debug("creating/updating player data table for " + pluginName);
            try (final Connection conn = getConnectionForPluginName(pluginName)) {

                createBaseTable(conn, pluginName);
                addMissingColumns(conn, pluginName);

            } catch (final SQLException exception) {
                CrownCore.log.warn("error creating/updating player data table:");
                CrownCore.log.warn(" - plugin: " + pluginName);
                CrownCore.log.warn(" - exception: " + exception.getMessage());
            }
        });
    }

    /***
     * creates a base table with a player uuid column
     * @param conn connection
     * @param pluginName pluginName
     * @throws SQLException sqlException
     */
    private void createBaseTable(final Connection conn, final String pluginName) throws SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS %s (
            PLAYER_UUID CHAR(36) PRIMARY KEY
        );
        """.formatted(pluginName.toUpperCase());

        try (final Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /***
     * creates the missing columns. compares schema to actual table.
     * @param conn connection
     * @param pluginName pluginName
     * @throws SQLException sqlException
     */
    private void addMissingColumns(final Connection conn, final String pluginName) throws SQLException {
        CrownCore.log.debug("creating missing columns");
        final Set<String> existingColumns = new HashSet<>();

        final DatabaseMetaData metaData = conn.getMetaData();

        try (final ResultSet columns = metaData.getColumns(null, "PUBLIC", pluginName.toUpperCase(), null)) {
            while (columns.next()) {
                final String column = columns.getString("COLUMN_NAME").toUpperCase();

                CrownCore.log.debug(" - found column " +column);
                existingColumns.add(column);
            }
        }

        for (final DataKey<?> key : playerDataSchemas.get(pluginName).getDataKeys()) {
            final String columnName = key.getName().toUpperCase();

            CrownCore.log.debug(" - checking column: " + columnName);

            if (existingColumns.contains(columnName)) {
                CrownCore.log.debug("   - already exists: " + columnName);
                continue;
            }

            final String alterSql = "ALTER TABLE " + pluginName +
                    " ADD COLUMN " + columnName + " " + key.getSqlDataType();

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(alterSql);
                CrownCore.log.debug("   - added missing column '" + columnName + "' to " + pluginName);
            }
        }
    }

    public Connection getConnectionForPluginName(final String name) throws SQLException {

        if(connections.isEmpty() || !connections.containsKey(name))
            throw new SQLException("Missing connection for " + name);

        return connections.get(name).getConnection();
    }

    public ConnectionSource getConnectionSourceForPlugin(final Plugin plugin) throws SQLException {
        final String pluginName = plugin.getName().toLowerCase();
        if(connections.isEmpty() || !connections.containsKey(pluginName))
            throw new SQLException("Missing connection for " + pluginName);

        final HikariDataSource hikariDataSource = connections.get(pluginName);

        return new JdbcPooledConnectionSource(
                hikariDataSource.getJdbcUrl(), hikariDataSource.getUsername(), hikariDataSource.getPassword()
        );
    }

    public void shutdown() {
        for (final HikariDataSource hikariDataSource : connections.values()) {
            try {
                hikariDataSource.close();
            } catch (Exception ignored) {}
        }
        connections.clear();
    }

    public void shutdownPluginConnections(final Plugin plugin) {
        final String pluginName = plugin.getName().toLowerCase();
        if(connections.containsKey(pluginName)) {
            CrownCore.log.debug("shutting down connection for " + pluginName);
            connections.computeIfPresent(pluginName, (name, con) -> {
                try {
                    if (!con.isClosed()) {
                        con.close();
                    }
                } catch (Exception e) {
                    CrownCore.log.warn("error closing connection for " + name + ": " + e.getMessage());
                }
                return null;
            });
        }
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

        CrownCore.log.debug(" - query: " + sql);

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

            CrownCore.log.debug(" - query: " + query);

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

        CrownCore.log.debug("calling player data load event");
        Scheduler.callEvent(CrownCore.getInstance(), new PlayerDataLoadEvent(playerData));
        CrownCore.log.debug("called player data load event");

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

        Scheduler.callEvent(CrownCore.getInstance(), new PlayerDataSaveEvent(playerData));

        return playerData;
    }
}

package de.obey.crown.core.data.plugin.storage;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.obey.crown.core.data.player.DataKey;
import de.obey.crown.core.data.player.DataKeyRegistry;
import de.obey.crown.core.data.player.PlayerData;
import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.noobf.CrownCore;

import java.nio.file.Path;
import java.sql.*;
import java.util.Map;
import java.util.UUID;

public class PluginStorageManager {

    private final Map<String, HikariDataSource> connections = Maps.newConcurrentMap();
    private final Map<String, PluginDataSchema> schemas = Maps.newConcurrentMap();

    public void registerPlayerDataPlugin(final CrownConfig pluginConfig) {
        final String pluginName = pluginConfig.getPlugin().getName();

        if(!DataKeyRegistry.pluginHasKeysw(pluginName)) {
            return;
        }

        final PluginStorageConfig storageConfig = pluginConfig.getPluginStorageConfig();
        final PluginDataSchema schema = new PluginDataSchema(pluginName);
        schemas.put(pluginName, schema);

        final HikariConfig hikariConfig = new HikariConfig();

        switch (storageConfig.getStorageType()) {
            case MYSQL, MARIADB -> {
                final String jdbcUrl = "jdbc:" + (storageConfig.getStorageType().name().toLowerCase()) + "://" + storageConfig.getHost() + "/" + storageConfig.getDatabase() +
                        "?autoReconnect=" + storageConfig.isAutoReconnect() +
                        "&useUnicode=" + storageConfig.isUseUnicode() +
                        "&characterEncoding=" + storageConfig.getCharacterEncoding() +
                        "&dontTrackOpenResources=" + storageConfig.isDontTrackOpenResources() +
                        "&holdResultsOpenOverStatementClose=" + storageConfig.isHoldResultsOpenOverStatementClose();

                hikariConfig.setJdbcUrl(jdbcUrl);
                hikariConfig.setUsername(storageConfig.getUsername());
                hikariConfig.setPassword(storageConfig.getPassword());
            }

            case H2 -> {
                final Path h2DataFile = pluginConfig.getPlugin().getDataFolder().toPath().resolve("playerData");
                final String jdbcUrl = "jdbc:h2:" + h2DataFile.toAbsolutePath();
                hikariConfig.setJdbcUrl(jdbcUrl);
                hikariConfig.setUsername("obey");
                hikariConfig.setPassword("");
            }

            default -> CrownCore.log.warn("invalid storage.method for " + pluginName + " in config.yml");

        }

        hikariConfig.setPoolName("Obey-" + pluginName);

        connections.put(pluginName, new HikariDataSource(hikariConfig));

        createTable(pluginName);
    }

    private void createTable(final String pluginName) {
        final StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        stringBuilder.append(pluginName).append(" (");
        stringBuilder.append("player_uuid CHAR(36) PRIMARY KEY, ");
        for (final DataKey<?> key : schemas.get(pluginName).getDataKeys()) {
            stringBuilder.append(key.getName()).append(" ").append(key.getSqlDataType()).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        stringBuilder.append(");");

        try (final Connection conn = getConnectionForPluginName(pluginName);
             final Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(stringBuilder.toString());
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
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

    private void insertDefaultValues(final UUID uuid, final PluginDataSchema schema) throws SQLException {
        final String plugiName = schema.getPluginName();
        final StringBuilder keys = new StringBuilder("player_uuid");
        final StringBuilder values = new StringBuilder("?");

        for (final DataKey<?> key : schema.getDataKeys()) {
            keys.append(", ").append(key.getName());
            values.append(", ?");
        }

        final String sql = "INSERT INTO " + plugiName+ " (" + keys + ") VALUES (" + values + ")";

        try (final Connection conn = getConnectionForPluginName(plugiName);
             final PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            int index = 2;
            for (final DataKey<?> key : schema.getDataKeys()) {
                stmt.setObject(index++, key.getDefaultValue());
            }

            stmt.executeUpdate();
        }
    }

    public PlayerData loadPlayerData(final PlayerData playerData) {
        for (final String pluginName : DataKeyRegistry.getRegistry().keySet()) {
            CrownCore.log.debug("loading player data for plugin: " + pluginName);
            final PluginDataSchema schema = schemas.get(pluginName);
            final StringBuilder query = new StringBuilder("SELECT * FROM ").append(pluginName).append(" WHERE player_uuid = ?");

            try (final Connection connection = getConnectionForPluginName(pluginName);
                 final PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

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
                        insertDefaultValues(playerData.getUuid(), schema);
                        schema.getDataKeys().forEach(key -> playerData.getData().put(key, key.getDefaultValue()));
                    }
                }
            } catch (final SQLException exception) {
                CrownCore.log.warn("Exception in PluginStorageManager 151");
                exception.printStackTrace();
            }
        }

        return playerData;
    }

    public PlayerData savePlayerData(final PlayerData playerData) {
        for (final String pluginName : DataKeyRegistry.getRegistry().keySet()) {
            final PluginDataSchema schema = schemas.get(pluginName);
            final StringBuilder query = new StringBuilder("UPDATE ").append(pluginName).append(" SET ");

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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return playerData;
    }
}

package de.obey.crown.core.data.player.newer;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Getter @Setter @SerializableAs("PlayerData")
public class PlayerData implements ConfigurationSerializable {

    private final ExecutorService executor = CrownCore.getInstance().getExecutor();
    private final Map<DataKey<?>, Object> data = Maps.newConcurrentMap();

    private Player player;
    private final UUID uuid;
    private boolean unload = false;
    private long lastseen = System.currentTimeMillis();

    public static PlayerData deserialize(Map<String, Object> map) {
        Log.debug(" - deserializing playerdata object");

        for (final String key : map.keySet()) {
            Log.debug("  -> key: " + key + " / value: " + map.get(key));
        }

        if (!map.containsKey("uuid"))
            return null;

        final UUID uuid = UUID.fromString((String) map.get("uuid"));
        final PlayerData playerData = new PlayerData(uuid);

        for (final DataKey<?> key : DataKeyRegistry.getRegistryValues()) {
            final Object raw = map.getOrDefault(key.path(), key.defaultValue());

            try {
                final Object value = key.type().cast(raw);
                playerData.getData().put(key, value);
                Log.debug("  - loaded key: " + key.key() + " = " + value);
            } catch (ClassCastException e) {
                Log.debug("  - type mismatch for key: " + key.key() + " (expected: " + key.type().getSimpleName() + ")");
            }
        }

        return playerData;
    }

    public static PlayerData valueOf(Map<String, Object> map) {
        return deserialize(map);
    }

    public static PlayerData create(final UUID uuid) {
        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "playerData/" + uuid + ".yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        Log.debug("creating playerdata object for '" + uuid + "'");

        if (!configuration.contains("data")) {
            Log.debug(" - new player, creating data");
            final PlayerData data = new PlayerData(uuid);

            for (final DataKey<?> dataKey : DataKeyRegistry.getRegistryValues()) {
                data.getData().put(dataKey, getDefault(dataKey.type()));
                Log.debug(" - set key '" + dataKey.key() + "' to default value: " + getDefault(dataKey.type()));
            }
            return data;
        }

        Log.debug(" - starting deserialization");

        Log.debug(configuration.getSerializable("data", PlayerData.class).toString());

        return configuration.getSerializable("data", PlayerData.class, new PlayerData(uuid));
    }

    /***
     * Executed whenever a player joins the server in the
     * PlayerJoinEvent. Used to update the player object and make sure
     * no data is unloaded when the player is online.
     *
     * @param player instance of Player
     */
    public void join(final Player player) {
        unload = false;
        lastseen = System.currentTimeMillis();
        this.player = player;
    }

    /*
    public void load() {
        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "playerData/" + uuid + ".yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if(!configuration.contains("data"))
            return;

        for (DataKey<?> key : DataKeyRegistry.getRegistryValues()) {
            final String path = "data." + key.path();

            if(!configuration.contains(path)) {
                configuration.set(path, getDefault(key.type()));
                continue;
            }

            final Object value = configuration.get(path);

            if(value == null)
                continue;

            if (!key.type().isInstance(value))
                continue;

            data.put(key, value);
        }

        FileUtil.saveConfigurationIntoFile(configuration, file);
    }

     */

    public PlayerData save() {
        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "playerData/" + uuid + ".yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        Log.debug("saving playerdata object");

        final Map<String, Object> serialized = serialize();

        for (String key : serialized.keySet())
            Log.debug("serialized - " + key + " : " + serialized.get(key));

        configuration.set("data", this);

        FileUtil.saveConfigurationIntoFile(configuration, file);

        return this;
    }

    public <T> void set(DataKey<T> key, T value) {
        data.put(key, value);
    }

    public <T> T get(DataKey<T> key) {
        return key.type().cast(data.getOrDefault(key, getDefault(key.type())));
    }

    private static Object getDefault(Class<?> type) {
        for (DataKey<?> key : DataKeyRegistry.getRegistryValues()) {
            if (type == key.type())
                return key.defaultValue();
        }

        return null;
    }

    public PlayerData setUnload(final boolean value) {
        this.unload = value;
        return this;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> serialized = Maps.newConcurrentMap();

        serialized.put("uuid", uuid.toString());

        for (final DataKey<?> dataKey : DataKeyRegistry.getRegistryValues())
            serialized.put(dataKey.path(), data.getOrDefault(dataKey, getDefault(dataKey.type())));

        return serialized;
    }
}

package de.obey.crown.core.data.player.newer;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Getter @Setter @SerializableAs("PlayerData")
public class PlayerData {

    private final ExecutorService executor = CrownCore.getInstance().getExecutor();
    private final Map<DataKey<?>, Object> data = Maps.newConcurrentMap();

    private Player player;
    private final UUID uuid;
    private boolean unload = false;
    private long lastseen = System.currentTimeMillis();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        load();
    }

    /***
     * loading playerdata from file.
     * defaults all registered datakeys if not present.
     */
    public void load() {
        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "playerData/" + uuid + ".yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if(!configuration.contains("data")) {
            CrownCore.log.debug(" - new player, creating data");

            for (final DataKey<?> dataKey : DataKeyRegistry.getRegistryValues()) {
                data.put(dataKey, getDefault(dataKey.type()));
                CrownCore.log.debug(" - set key '" + dataKey.key() + "' to default value: " + getDefault(dataKey.type()));
            }
            return;
        }

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

    /***
     * saves all datakeys and their values into the file.
     * path: data.'datakey.path'
     *
     * @return current PlayerData
     */
    public PlayerData save() {
        final File file = FileUtil.getGeneratedFile(CrownCore.getInstance(), "playerData/" + uuid + ".yml", true);
        final YamlConfiguration configuration = new YamlConfiguration();

        CrownCore.log.debug("saving playerdata '" + uuid + "'");

        for (DataKey<?> dataKey : data.keySet()) {
            configuration.set("data." + dataKey.path(), get(dataKey));
        }

        FileUtil.saveConfigurationIntoFile(configuration, file);
        return this;
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

    /***
     * set the value of a datakey in the data cache
     * @param key dataKey to set
     * @param value value to set
     * @param <T> type of dateKey
     */
    public <T> void set(DataKey<T> key, T value) {
        data.put(key, value);
    }

    /***
     *
     * @param key dataKey to get
     * @return value stored in data cache for dataKey
     * @param <T> type of dateKey
     */
    public <T> T get(DataKey<T> key) {
        return key.type().cast(data.getOrDefault(key, getDefault(key.type())));
    }

    /***
     * get the default value for a dataKey
     * @param type type of dataKey
     * @return default value stored in dataKey
     */

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
}

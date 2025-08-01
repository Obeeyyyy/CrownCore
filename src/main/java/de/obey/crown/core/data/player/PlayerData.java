package de.obey.crown.core.data.player;

import com.google.common.collect.Maps;
import de.obey.crown.core.data.plugin.storage.PluginStorageManager;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Getter @Setter @SerializableAs("PlayerData")
public class PlayerData {

    private final ExecutorService executor = CrownCore.getInstance().getExecutor();
    private final PluginStorageManager pluginStorageManager = CrownCore.getInstance().getPluginStorageManager();

    private final Map<DataKey<?>, Object> data = Maps.newConcurrentMap();
    private final Map<String, Object> runtimeData = Maps.newConcurrentMap();

    private Player player;
    private final UUID uuid;
    private boolean unload = false;
    private long lastseen = System.currentTimeMillis();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        load();
    }


    public void load() {
        pluginStorageManager.loadPlayerData(this);
    }

    /***
     * saves all data keys and their values.
     *
     * @return current PlayerData
     */
    public PlayerData save() {
        return pluginStorageManager.savePlayerData(this);
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
        return key.getDataType().cast(data.getOrDefault(key, key.getDefaultValue()));
    }

    /***
     * marks the player data instance to be unloaded from cache in the next iteration
     * @param value true or false
     * @return player data instance
     */
    public PlayerData setUnload(final boolean value) {
        this.unload = value;
        return this;
    }


    public void setRuntimeData(final String key, final Object value) {
        runtimeData.put(key, value);
    }

    public Object getRuntimeData(final String key) {
        return runtimeData.get(key);
    }

}

package de.obey.crown.core.data.plugin.storage.datakey;

import de.obey.crown.core.noobf.CrownCore;
import org.bukkit.plugin.Plugin;

public class PlayerDataKey<T> extends DataKey<T> {

    public PlayerDataKey(String name, Plugin plugin, Class<T> dataType, T defaultValue, String sqlDataType) {
        super(name, plugin, dataType, defaultValue, sqlDataType);

        CrownCore.log.debug("CREATED PLAYERDATA KEY " + name);
        DataKeyRegistry.register(this);
    }

    public PlayerDataKey(String name, Plugin plugin, Class<T> dataType, String sqlDataType) {
        super(name, plugin, dataType, sqlDataType);

        CrownCore.log.debug("CREATED PLAYERDATA KEY " + name);
        DataKeyRegistry.register(this);
    }
}

package de.obey.crown.core.data.plugin.storage.datakey;

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class DataUtil {

    public CompletableFuture<Void> addLong(final UUID uuid, final long amount, final DataKey<Long> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) + amount));
    }

    public CompletableFuture<Void> addDouble(final UUID uuid, final double amount, final DataKey<Double> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) + amount));
    }

    public CompletableFuture<Void> addInteger(final UUID uuid, final int amount, final DataKey<Integer> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) + amount));
    }

    public CompletableFuture<Void> removeLong(final UUID uuid, final long amount, final DataKey<Long> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) - amount));
    }

    public CompletableFuture<Void> removeDouble(final UUID uuid, final double amount, final DataKey<Double> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) - amount));
    }

    public CompletableFuture<Void> removeInteger(final UUID uuid, final int amount, final DataKey<Integer> dataKey) {
        return CrownCore.getInstance().getPlayerDataService().loadAsync(uuid).thenAccept((data) -> data.set(dataKey, data.get(dataKey) - amount));
    }

}

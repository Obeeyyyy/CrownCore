package de.obey.crown.core.data.v1.api;


/*
    Author: Obey
    Date: 02.04.2026
    Time: 10:03
    Project: CrownCore
*/

import de.obey.crown.core.data.v1.exception.PlayerDataNotFoundException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ICrownPlayerDataService<T, ID> extends ICrownService {

    CompletableFuture<T> load(final ID id);

    Optional<T> get(final ID id);

    T getOrThrow(final ID id) throws PlayerDataNotFoundException;

    boolean isLoaded(final ID id);

    void edit(final ID id, final Consumer<T> consumer);

    CompletableFuture<Void> unload(final ID id);

    CompletableFuture<Void> save(final ID id);

    void clearCache();

}

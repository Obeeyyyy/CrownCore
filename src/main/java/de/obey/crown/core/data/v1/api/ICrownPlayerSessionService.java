package de.obey.crown.core.data.v1.api;


/*
    Author: Obey
    Date: 02.04.2026
    Time: 15:01
    Project: CrownCore
*/

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICrownPlayerSessionService<S extends ICrownPlayerSession<S>, ID extends UUID> extends ICrownService {

    @Override
    void saveAllAsync();

    @Override
    void saveAllSync();

    CompletableFuture<S> load(final ID id);

    CompletableFuture<Void> unload(final ID id);

    CompletableFuture<S> save(final ID id);

    S get(final ID id);


}

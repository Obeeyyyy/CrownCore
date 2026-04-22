package de.obey.crown.core.data.v1.api;

import java.util.concurrent.CompletableFuture;

public interface ICrownPlayerSession<S> {

    CompletableFuture<S> loadAsync();
    CompletableFuture<S> saveAsync();
    S save();
}

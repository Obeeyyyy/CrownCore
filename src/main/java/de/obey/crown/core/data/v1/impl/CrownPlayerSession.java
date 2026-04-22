package de.obey.crown.core.data.v1.impl;


/*
    Author: Obey
    Date: 15.04.2026
    Time: 14:30
    Project: CrownCore
*/

import de.obey.crown.core.data.v1.api.ICrownPlayerSession;
import de.obey.crown.core.noobf.CrownCore;
import lombok.*;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Getter @Setter
public abstract class CrownPlayerSession<S> implements ICrownPlayerSession<S> {

    protected final Executor executor = CrownCore.getInstance().getExecutor();
    protected final UUID uuid;

    protected Player player;
    protected long lastSeen;

    public abstract CompletableFuture<S> loadAsync();
    public abstract CompletableFuture<S> saveAsync();
    public abstract S save();

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }
}

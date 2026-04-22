package de.obey.crown.core.listener;

import de.obey.crown.core.data.plugin.storage.player.PlayerDataService;
import de.obey.crown.core.noobf.CrownCore;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

@RequiredArgsConstructor
public class PlayerLogin implements Listener {

    private final PlayerDataService playerDataService;

    @EventHandler
    public void on(final AsyncPlayerPreLoginEvent event) {

        if(!CrownCore.getInstance().isReady()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Server Starting"));
            return;
        }

        playerDataService.loadAsync(event.getUniqueId());
    }
}

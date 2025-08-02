package de.obey.crown.core.listener;

import de.obey.crown.core.data.plugin.storage.player.PlayerDataService;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

@RequiredArgsConstructor
public class PlayerLogin implements Listener {

    private final PlayerDataService playerDataService;

    @EventHandler
    public void on(final PlayerLoginEvent event) {
        playerDataService.loadAsync(event.getPlayer().getUniqueId());
    }
}

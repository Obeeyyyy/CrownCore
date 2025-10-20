package de.obey.crown.core.listener;

import de.obey.crown.core.data.plugin.storage.player.PlayerDataService;
import de.obey.crown.core.noobf.PluginConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuit implements Listener {

    private final PluginConfig pluginConfig;
    private final PlayerDataService playerDataService;

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        playerDataService.saveAsync(event.getPlayer().getUniqueId()).thenRun(() -> {
            if(pluginConfig.getDataCacheTime() <= 0) {
                playerDataService.unloadFromCache(event.getPlayer().getUniqueId());
            }
        });
    }

}

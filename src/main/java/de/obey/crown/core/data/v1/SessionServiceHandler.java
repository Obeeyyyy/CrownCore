package de.obey.crown.core.data.v1;


/*
    Author: Obey
    Date: 15.04.2026
    Time: 14:44
    Project: CrownCore
*/

import com.google.common.collect.Maps;
import de.obey.crown.core.data.v1.api.ICrownPlayerSession;
import de.obey.crown.core.data.v1.api.ICrownPlayerSessionService;
import de.obey.crown.core.data.v1.impl.CrownPlayerSession;
import de.obey.crown.core.data.v1.impl.CrownPlayerSessionService;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.noobf.PluginConfig;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.task.CrownTask;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

public class SessionServiceHandler implements Listener {

    private final Map<Plugin, CrownPlayerSessionService<?, UUID>> sessionServiceMap = Maps.newConcurrentMap();

    @Getter
    private final CrownTask sessionScheduler;

    public SessionServiceHandler(final Plugin plugin, final PluginConfig pluginConfig) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        sessionScheduler = Scheduler.runGlobalTaskTimer(plugin, () -> {

            CrownCore.log.info("session handler task");
            CrownCore.log.info(" - " + sessionServiceMap.size() + " session services");

            for (CrownPlayerSessionService<?, UUID> sessionService : sessionServiceMap.values()) {
                CrownCore.log.info("   -> session service: " + sessionService.getPlugin().getName());
                CrownCore.log.info("     - sessions: " + sessionService.getSessions().size());
                sessionService.saveAllAsync();

                for (final CrownPlayerSession<?> session : sessionService.getSessions().values()) {
                    CrownCore.log.info("       -> session for player: " + session.getPlayer().map(Player::getName).orElse("loading") + " (" + session.getUuid() + ")");

                    if(session.getPlayer().map(Player::isOnline).orElse(false))  {
                        if(System.currentTimeMillis() - session.getLastSeen() >= pluginConfig.getDataCacheTime()) {
                            sessionService.unload(session.getUuid());
                        }
                    }

                }
            }

        }, 20, 20*120);
    }

    public void saveAllSync() {
        for (final CrownPlayerSessionService<?, UUID> sessionService : sessionServiceMap.values()) {
            sessionService.saveAllSync();
        }
    }

    @EventHandler
    public void on(final AsyncPlayerPreLoginEvent event) {
        if(sessionServiceMap.isEmpty())
            return;

        for (final CrownPlayerSessionService<?, UUID> sessionService : sessionServiceMap.values()) {
            sessionService.load(event.getUniqueId());
        }
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        if(sessionServiceMap.isEmpty())
            return;

        for (final CrownPlayerSessionService<?, UUID> sessionService : sessionServiceMap.values()) {
            sessionService.load(event.getPlayer().getUniqueId()).thenAcceptAsync(( session) -> session.setPlayer(event.getPlayer()));
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        if(sessionServiceMap.isEmpty())
            return;

        for (final CrownPlayerSessionService<?, UUID> sessionService : sessionServiceMap.values()) {
            sessionService.save(event.getPlayer().getUniqueId());
        }
    }

    public <S extends CrownPlayerSession<S>, ID extends UUID> void registerSessionService(final CrownPlayerSessionService<S,ID> sessionService) {
        CrownCore.log.info("registering session service for " + sessionService.getPlugin().getName());
        sessionServiceMap.put(sessionService.getPlugin(), (CrownPlayerSessionService<?, UUID>) sessionService);
    }
}

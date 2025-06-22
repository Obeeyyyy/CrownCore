/* CrownPlugins - CrownCore */
/* 22.04.2025 - 12:51 */

package de.obey.crown.core.listener;

import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.noobf.PluginConfig;
import de.obey.crown.core.data.player.newer.PlayerDataService;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.VersionChecker;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public final class PlayerJoin implements Listener {

    private final PluginConfig pluginConfig;
    private final VersionChecker versionChecker;
    private final PlayerDataService playerDataService;

    @EventHandler
    public void on(final PlayerJoinEvent event) {

        playerDataService.loadAsync(event.getPlayer().getUniqueId()).thenAccept((data) -> data.join(event.getPlayer()));

        if (!pluginConfig.isUpdateReminder())
            return;

        if (!event.getPlayer().hasPermission("core.admin"))
            return;

        if (versionChecker.getOutdatedPlugins().isEmpty())
            return;

        final Player player = event.getPlayer();

        Scheduler.runTaskLater(CrownCore.getInstance(), () -> {
            for (final Plugin plugin : versionChecker.getOutdatedPlugins()) {
                final String pluginName = plugin.getName();
                final String newest = versionChecker.getNewestVersions().get(pluginName);
                final String current = plugin.getDescription().getVersion();
                final String downloadLink = plugin.getDescription().getWebsite();

                player.sendMessage("");
                pluginConfig.getMessanger().sendNonConfigMessage(
                        player,
                        "%prefix% Your version of %accent%&n" + pluginName + "%white% is &coutdated%white%!");

                pluginConfig.getMessanger().sendNonConfigMessage(player, "            &8➥ &7current: &c" + current + "&7 - latest: &a" + newest);
                pluginConfig.getMessanger().sendNonConfigMessage(player, "            &8➥ &7Download: &n" + (downloadLink == null ? "https://builtbybit.com/creators/crown-plugins.427256" : downloadLink));
                player.sendMessage("");
            }
        }, 20);

    }

}

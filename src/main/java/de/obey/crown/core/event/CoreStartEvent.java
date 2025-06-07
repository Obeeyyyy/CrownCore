/* CrownPlugins - CrownCore */
/* 18.08.2024 - 00:18 */

package de.obey.crown.core.event;

import de.obey.crown.core.util.VersionChecker;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public final class CoreStartEvent extends Event {

    private final VersionChecker versionChecker;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public void sendStartupMessage(final Plugin plugin) {
        final boolean isLatest = versionChecker.isNewestVersion(plugin);

        Component component = Component.empty()
                .append(Component.text("\n")
                        .append(Component.text("                 ✔", NamedTextColor.GREEN)))
                .append(Component.text(" Enabled ", NamedTextColor.WHITE))
                .append(Component.text(plugin.getName(), NamedTextColor.DARK_PURPLE))
                .append(Component.text(" - ", NamedTextColor.WHITE))
                .append(Component.text("v" + plugin.getDescription().getVersion(), NamedTextColor.DARK_PURPLE))
                .append(isLatest ? Component.text(" (latest) \n", NamedTextColor.GREEN) : Component.text(" (outdated)\n", NamedTextColor.DARK_RED));

        if (!isLatest) {
            component = component.append(Component.text("                       ! Version: v" + versionChecker.getNewestVersion(plugin) + " is available.\n", NamedTextColor.YELLOW));
            versionChecker.getOutdatedPlugins().add(plugin);
        }

        Bukkit.getConsoleSender().sendMessage(component);
    }

}

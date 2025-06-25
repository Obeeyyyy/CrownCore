/* CrownPlugins */
/* 26.03.2025 - 05:08 */

package de.obey.crown.core.data.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.naming.Name;

@Setter @Getter
public class Log {

    private boolean debug = true;
    private Plugin plugin;

    public void info(final String string) {
        final Component message = Component.empty()
                .append(Component.text("[" + plugin.getName() + "]").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text(" <INFO> ").color(NamedTextColor.GREEN))
                .append(Component.text(string).color(NamedTextColor.WHITE));

        Bukkit.getConsoleSender().sendMessage(message);
    }

    public void warn(final String string) {
        final Component message = Component.empty()
                .append(Component.text("[" + plugin.getName() + "]").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text(" <WARNING> ").color(NamedTextColor.DARK_RED))
                .append(Component.text(string).color(NamedTextColor.RED));

        Bukkit.getConsoleSender().sendMessage(message);
    }

    public void debug(final String string) {
        if(!debug)
            return;

        final Component message = Component.empty()
                .append(Component.text("[" + plugin.getName() + "]").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text(" <DEBUGGER> ").color(NamedTextColor.YELLOW))
                .append(Component.text(string).color(NamedTextColor.YELLOW));

        Bukkit.getConsoleSender().sendMessage(message);
    }
}

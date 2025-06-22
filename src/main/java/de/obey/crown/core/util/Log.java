/* CrownPlugins */
/* 26.03.2025 - 05:08 */

package de.obey.crown.core.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public final class Log {

    @Setter @Getter
    private boolean debug = false;

    public void info(final String string) {
        Bukkit.getLogger().info("[OBEY] " + string);
    }

    public void warn(final String string) {
        Bukkit.getLogger().warning("[OBEY] " + string);
    }

    public void debug(final String string) {
        if(debug)
            Bukkit.getLogger().info("[OBEY] <DEBUGGER> " + string);
    }
}

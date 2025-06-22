/* CrownPlugins - CrownCore */
/* 22.04.2025 - 13:05 */

package de.obey.crown.core.listener;

import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.event.CoreStartEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public final class CoreStart implements Listener {

    @EventHandler
    public void on(final CoreStartEvent event) {
        event.sendStartupMessage(CrownCore.getInstance());
    }
}

/* CrownPlugins - CrownCore */
/* 15.04.2025 - 22:58 */

package de.obey.crown.core.util;

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ExecutorService;

@UtilityClass
public final class Scheduler {

    private  ExecutorService executor;
    private boolean isFolia = false;

    public void initialize() {
        if (Bukkit.getVersion().contains("Folia"))
            isFolia = true;

        executor = CrownCore.getInstance().getExecutor();
    }

    public void callEvent(final Plugin plugin, final Event event) {
        if (isFolia) {

            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                Bukkit.getPluginManager().callEvent(event);
            });

            return;
        }
    }

    public void runTask(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runTaskAsync(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, (scheduledTask) -> executor.execute(task));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runTaskLater(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (scheduledTask) -> task.run(), delay);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public void runTaskLaterAsync(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (scheduledTask) -> executor.execute(task), delay);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public void runTaskTimer(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> task.run(), delay, period);
            return;
        }
        
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public void runTaskTimerAsync(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> executor.execute(task), delay, period);
            return;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }

}

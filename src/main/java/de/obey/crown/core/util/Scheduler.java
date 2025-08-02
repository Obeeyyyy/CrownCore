/* CrownPlugins - CrownCore */
/* 15.04.2025 - 22:58 */

package de.obey.crown.core.util;

import de.obey.crown.core.noobf.CrownCore;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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
        if (CrownCore.getInstance().getServer().getName().toLowerCase().contains("folia") ||
                CrownCore.getInstance().getServer().getName().toLowerCase().contains("luminol")) {
            isFolia = true;

            CrownCore.log.debug("detected folia/luminol server");
        }

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

    public Runnable runTask(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
            return task;
        }

        Bukkit.getScheduler().runTask(plugin, task);
        return task;
    }

    public Runnable runTaskAsync(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, (scheduledTask) -> executor.execute(task));
            return task;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        return task;
    }

    public Runnable runTaskLater(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (scheduledTask) -> task.run(), delay);
            return task;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return task;
    }

    public Runnable runTaskLaterAsync(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (scheduledTask) -> executor.execute(task), delay);
            return task;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return task;
    }

    public Runnable runTaskTimer(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> task.run(), delay, period);
            return task;
        }
        
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return task;
    }

    public Runnable runTaskTimerAsync(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> executor.execute(task), delay, period);
            return task;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        return task;
    }

}

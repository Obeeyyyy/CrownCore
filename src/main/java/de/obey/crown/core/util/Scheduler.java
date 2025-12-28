/* CrownPlugins - CrownCore */
/* 15.04.2025 - 22:58 */

package de.obey.crown.core.util;

import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.task.CrownTask;
import de.obey.crown.core.util.task.CrownTaskImpl;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class Scheduler {

    private boolean isFolia = false;

    public void initialize() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            CrownCore.log.debug("detected folia server");
            isFolia = true;
        } catch (final ClassNotFoundException ignored) {
            isFolia = false;
        }
    }

    //region events

    public CrownTask callEvent(final Plugin plugin, final Event event) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask ->
                    Bukkit.getPluginManager().callEvent(event));

            return new CrownTaskImpl(st);
        }

        final BukkitTask bt = Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getPluginManager().callEvent(event));

        return new CrownTaskImpl(bt);
    }

    //endregion

    //region globaltasks

    public CrownTask runGlobalTask(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());

            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTask(plugin, task));
    }

    public CrownTask runGlobalTaskLater(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
    }

    public CrownTask runGlobalTaskTimer(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            ScheduledTask st = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
            return new CrownTaskImpl(st);
        }

        return new CrownTaskImpl(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
    }

    //endregion

    //region entity tasks

    public CrownTask runEntityTask(final Plugin plugin, final Entity entity, final Runnable task) {
        if (isFolia) {
            final boolean scheduled = entity.getScheduler().execute(plugin, task, null, 0L);
            return new CrownTask() {
                @Override
                public void cancel() { /* nothing to cancel */ }

                @Override
                public boolean isCancelled() { return !scheduled; }
            };
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTask(plugin, task));
    }

    public CrownTask runEntityTaskLater(final Plugin plugin, final Entity entity, final Runnable task, final long delay) {
        if (isFolia) {
            final ScheduledTask st = entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
    }

    public CrownTask runEntityTaskTimer(final Plugin plugin, final Entity entity, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            final ScheduledTask st = entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), null, delay, period);
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
    }

    //endregion

    //region location tasks

    public CrownTask runLocationTask(final Plugin plugin, final Location location, final Runnable task) {
        if (isFolia) {
            Bukkit.getRegionScheduler().execute(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
            return new CrownTask() {
                @Override
                public void cancel() { /* nothing to cancel */ }

                @Override
                public boolean isCancelled() { return false; }
            };
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTask(plugin, task));
    }

    public CrownTask runLocationTaskLater(final Plugin plugin, final Location location, final Runnable task, final long delay) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getRegionScheduler().runDelayed(
                    plugin,
                    location.getWorld(),
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4,
                    scheduledTask -> task.run(),
                    delay
            );

            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
    }

    public CrownTask runLocationTaskTimer(final Plugin plugin, final Location location, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getRegionScheduler().runAtFixedRate(
                    plugin,
                    location.getWorld(),
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4,
                    scheduledTask -> task.run(),
                    delay,
                    period
            );

            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
    }

    //endregion

    //region async tasks

    public CrownTask runAsyncTask(final Plugin plugin, final Runnable task) {
        if (isFolia) {
            final ScheduledTask st = Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public CrownTask runAsyncTaskLater(final Plugin plugin, final Runnable task, final long delay) {
        if (isFolia) {
            final long millis = delay * 50;
            final ScheduledTask st = Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), millis, TimeUnit.MILLISECONDS);
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
    }

    public CrownTask runAsyncTaskTimer(final Plugin plugin, final Runnable task, final long delay, final long period) {
        if (isFolia) {
            final long millisDelay = delay * 50;
            final long millisPeriod = period * 50;
            final ScheduledTask st = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), millisDelay, millisPeriod, TimeUnit.MILLISECONDS);
            return new CrownTaskImpl(st);
        }
        return new CrownTaskImpl(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
    }

    //endregion

}

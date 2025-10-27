package de.obey.crown.core.util.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class CrownTaskImpl implements CrownTask{

    private final BukkitTask bukkitTask;
    private final ScheduledTask foliaTask;

    public CrownTaskImpl(final BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
        this.foliaTask = null;
    }

    public CrownTaskImpl(final ScheduledTask foliaTask) {
        this.foliaTask = foliaTask;
        this.bukkitTask = null;
    }

    @Override
    public void cancel() {
        if (bukkitTask != null) bukkitTask.cancel();
        if (foliaTask != null) foliaTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        if (bukkitTask != null) return bukkitTask.isCancelled();
        if (foliaTask != null) return foliaTask.isCancelled();

        return true;
    }
}

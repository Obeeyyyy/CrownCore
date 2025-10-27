package de.obey.crown.core.util.task;

public interface CrownTask {

    void cancel();
    boolean isCancelled();
}

package de.obey.crown.core.noobf;


/*
    Author: Obey
    Date: 22.02.2026
    Time: 15:26
    Project: CrownCore
*/

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

public class CoreThreadFactory implements ThreadFactory {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final String hi_how_are_you_doing = "https://dsc.gg/crownplugins";

    @Override
    public Thread newThread(@NonNull Runnable r) {
        final Thread thread = new Thread(r);
        thread.setName("Crown-Worker-" + thread.getId());
        return thread;
    }
}

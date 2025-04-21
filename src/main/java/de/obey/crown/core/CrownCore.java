package de.obey.crown.core;

import de.obey.crown.core.command.CoreCommand;
import de.obey.crown.core.command.LocationCommand;
import de.obey.crown.core.event.CoreStartEvent;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.listener.PlayerChat;
import de.obey.crown.core.listener.PlayerCommandPreprocess;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.Teleporter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public final class CrownCore extends JavaPlugin {

    private ExecutorService executorService;

    private boolean placeholderapi = false;

    private PluginConfig pluginConfig;

    @Override
    public void onLoad() {
        executorService = Executors.newCachedThreadPool();
        pluginConfig = new PluginConfig(this);
    }

    @Override
    public void onEnable() {

        // check if placeholderapi is present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderapi = true;
            new Placeholders().register();
        }

        Scheduler.initialize();

        pluginConfig.getMessanger().loadCorePlaceholders();
        load();

        // load locations
        Scheduler.runTaskLater(this, () -> {
            LocationHandler.loadLocations();
            Teleporter.initialize();
            getServer().getPluginManager().callEvent(new CoreStartEvent());
        }, 2);
    }

    @Override
    public void onDisable() {
        LocationHandler.saveLocations();
    }

    public void load() {
        loadListener();
        loadCommand();
    }

    private void loadCommand() {
        final LocationCommand locationCommand = new LocationCommand(pluginConfig.getMessanger());
        getCommand("location").setExecutor(locationCommand);
        getCommand("location").setTabCompleter(locationCommand);

        final CoreCommand coreCommand = new CoreCommand(pluginConfig.getMessanger(), pluginConfig);
        getCommand("crowncore").setExecutor(coreCommand);
        getCommand("crowncore").setTabCompleter(coreCommand);
    }

    private void loadListener() {
        final PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerChat(pluginConfig), this);
        pluginManager.registerEvents(new PlayerCommandPreprocess(pluginConfig, pluginConfig.getMessanger()), this);
    }

    public static CrownCore getInstance() {
        return getPlugin(CrownCore.class);
    }
}

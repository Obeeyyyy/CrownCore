package de.obey.crown.core.noobf;

import de.obey.crown.core.command.CoreCommand;
import de.obey.crown.core.command.LocationCommand;
import de.obey.crown.core.data.plugin.Messanger;
import de.obey.crown.core.data.plugin.storage.player.PlayerDataService;
import de.obey.crown.core.data.plugin.Log;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.data.plugin.storage.PluginStorageManager;
import de.obey.crown.core.event.CoreStartEvent;
import de.obey.crown.core.gui.command.CrownGuiCommand;
import de.obey.crown.core.gui.listener.GuiClickListener;
import de.obey.crown.core.gui.listener.GuiCloseListener;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.listener.*;
import de.obey.crown.core.util.*;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import okhttp3.OkHttpClient;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public final class CrownCore extends JavaPlugin {

    public static final Log log = new Log();
    private static CrownCore crownCore;

    private ExecutorService executor;
    private OkHttpClient okHttpClient;
    private VersionChecker versionChecker;
    private PluginStorageManager pluginStorageManager;

    private PluginConfig pluginConfig;
    private Messanger messanger;
    private Sounds sounds;

    private PlayerDataService playerDataService;

    /***
     * Ran when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        crownCore = this;
        log.setPlugin(this);

        /***
         * Fixed thread pool.
         * Threads: 8
         * Used for all core and child plugins tasks.
         */
        executor = Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("CrownCore-Worker-" + thread.getId());
            return thread;
        });

        okHttpClient = new OkHttpClient();
        pluginStorageManager = new PluginStorageManager(executor);

        pluginConfig = new PluginConfig(this);
        messanger = pluginConfig.getMessanger();
        sounds = pluginConfig.getSounds();
    }

    /***
     * Ran when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        Scheduler.initialize();
        UUIDFetcher.initHTTPClient(okHttpClient);
        PlaceholderUtil.init();
        FloodgateUtil.init();

        versionChecker = new VersionChecker(executor, okHttpClient);
        versionChecker.retrieveNewestPluginVersions();
        playerDataService = new PlayerDataService(pluginConfig, executor);

        initializeBStats();
        initializeCore();
    }

    /***
     * Ran when the plugin is disabled. Saves Locations and shutdowns connections.
     */
    @Override
    public void onDisable() {
        LocationHandler.saveLocations();
        pluginStorageManager.shutdownConnections();
        executor.shutdown();
    }

    /***
     * Initializes metrics for bStats
     */
    private void initializeBStats()  {
        new Metrics(this, 27306);
    }

    /***
     * Loads commands and listener.
     */
    public void initializeCore() {
        loadListener();
        loadCommand();

        getPluginStorageManager().loadPluginDataPlugins();
        getPluginStorageManager().loadPlayerDataPlugins();

        Scheduler.runGlobalTaskLater(this, () -> {
            LocationHandler.loadLocations();
            Teleporter.initialize();
            sendConsoleMessage();
            getServer().getPluginManager().callEvent(new CoreStartEvent(versionChecker));
        }, 2);
    }

    /***
     * Loads commands.
     */
    private void loadCommand() {
        final LocationCommand locationCommand = new LocationCommand(messanger);
        getCommand("location").setExecutor(locationCommand);
        getCommand("location").setTabCompleter(locationCommand);

        final CoreCommand coreCommand = new CoreCommand(messanger, pluginConfig);
        getCommand("crowncore").setExecutor(coreCommand);
        getCommand("crowncore").setTabCompleter(coreCommand);

        final CrownGuiCommand crownGuiCommand = new CrownGuiCommand(messanger);
        getCommand("crowngui").setExecutor(crownGuiCommand);
        getCommand("crowngui").setTabCompleter(crownGuiCommand);
    }

    /***
     * Loads listener.
     */
    private void loadListener() {
        final PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new CoreStart(), this);
        pluginManager.registerEvents(new PlayerChat(pluginConfig, sounds), this);
        pluginManager.registerEvents(new PlayerJoin(pluginConfig, versionChecker, playerDataService), this);
        pluginManager.registerEvents(new PlayerLogin(playerDataService), this);
        pluginManager.registerEvents(new PlayerQuit(pluginConfig, playerDataService), this);
        pluginManager.registerEvents(new GuiClickListener(), this);
        pluginManager.registerEvents(new GuiCloseListener(), this);
    }

    /***
     * Returns the CrownCore (plugin) instance.
     * @return CrownCore Instance
     */
    public static CrownCore getInstance() {
        return crownCore;
    }


    /***
     * Sends the initial console message on startup.
     */
    private void sendConsoleMessage() {
        final String pluginName = this.getDescription().getName();
        final String version = this.getDescription().getVersion();
        final String serverVersion = Bukkit.getVersion();
        final TextColor orange = TextColor.fromHexString("#ffd138");

        final Component message = Component.empty()
                .append(Component.text("\n"))
                .append(Component.text("          ╔═════════════════════════════════════════════════╗").color(NamedTextColor.GREEN))
                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)))
                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)
                                .append(Component.text("                ______   _____ \n").color(orange))))
                .append(Component.text("          ║").color(NamedTextColor.GREEN)
                        .append(Component.text("               /\\  ___\\ /\\  __ \\ \n").color(orange)))
                .append(Component.text("          ║").color(NamedTextColor.GREEN)
                        .append(Component.text("               \\ \\ \\____\\ \\ \\/\\ \\ \n").color(orange)))
                .append(Component.text("          ║").color(NamedTextColor.GREEN)
                        .append(Component.text("                \\ \\_____\\\\ \\____/ \n").color(orange)))
                .append(Component.text("          ║").color(NamedTextColor.GREEN)
                        .append(Component.text("                 \\/_____/ \\/____/").color(orange)))
                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)))
                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)
                                .append(Component.text("            ♕    ", orange)
                                        .append(Component.text(pluginName + " v" + version + " \n").color(NamedTextColor.WHITE)))))
                .append(Component.text("          ╠─────────────────────────────────────────────────╣").color(NamedTextColor.GREEN))

                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)))

                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)
                                .append(Component.text("  Author: ").color(NamedTextColor.GRAY)
                                        .append(Component.text("@Obeeyyyy").color(NamedTextColor.WHITE)))))

                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)
                                .append(Component.text("  Discord: ").color(NamedTextColor.GRAY)
                                        .append(Component.text("https://discord.com/invite/bJQRV5GeZg").color(NamedTextColor.WHITE)))))

                .append(Component.text("\n")
                        .append(Component.text("          ║").color(NamedTextColor.GREEN)
                                .append(Component.text("  Server: ").color(NamedTextColor.GRAY)
                                        .append(Component.text(serverVersion).color(NamedTextColor.WHITE)))))

                .append(Component.text("\n")
                        .append(Component.text("          ╚═════════════════════════════════════════════════╝\n").color(NamedTextColor.GREEN)));


        Bukkit.getConsoleSender().sendMessage(message);
    }
}

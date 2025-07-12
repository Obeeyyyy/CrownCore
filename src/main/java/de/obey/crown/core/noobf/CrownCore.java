package de.obey.crown.core.noobf;

import de.obey.crown.core.command.CoreCommand;
import de.obey.crown.core.command.LocationCommand;
import de.obey.crown.core.data.player.newer.PlayerDataService;
import de.obey.crown.core.data.plugin.Log;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.event.CoreStartEvent;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.listener.*;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.Teleporter;
import de.obey.crown.core.util.UUIDFetcher;
import de.obey.crown.core.util.VersionChecker;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public final class CrownCore extends JavaPlugin {

    public static final Log log = new Log();
    private static CrownCore crownCore;

    /***
     * Fixed thread pool.
     * Threads: 8
     * Used for all core and child plugins tasks.
     */
    private ExecutorService executor;
    private OkHttpClient okHttpClient;
    private VersionChecker versionChecker;

    private boolean placeholderapi = false;

    private PluginConfig pluginConfig;
    private Sounds sounds;

    private PlayerDataService playerDataService;

    @Override
    public void onLoad() {
        crownCore = this;
        log.setPlugin(this);

        executor = Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Crown-Worker-" + thread.getId());
            return thread;
        });

        okHttpClient = new OkHttpClient();

        pluginConfig = new PluginConfig(this);
        sounds = pluginConfig.getSounds();
    }

    @Override
    public void onEnable() {
        Scheduler.initialize();
        UUIDFetcher.initHTTPClient(okHttpClient);

        // check if placeholderapi is present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderapi = true;
            new Placeholders().register();
        }

        versionChecker = new VersionChecker(executor, okHttpClient);
        versionChecker.retrieveNewestPluginVersions();
        playerDataService = new PlayerDataService(executor);

        load();

        // load locations
        Scheduler.runTaskLater(this, () -> {
            LocationHandler.loadLocations();
            Teleporter.initialize();
            sendConsoleMessage();
            getServer().getPluginManager().callEvent(new CoreStartEvent(versionChecker));
        }, 2);
    }

    @Override
    public void onDisable() {
        LocationHandler.saveLocations();

        if(playerDataService != null)
            playerDataService.saveAllData();
    }

    /***
     * Loads commands and listener.
     */
    public void load() {
        loadListener();
        loadCommand();
    }

    /***
     * Loads commands.
     */
    private void loadCommand() {
        final LocationCommand locationCommand = new LocationCommand(pluginConfig.getMessanger());
        Objects.requireNonNull(getCommand("location")).setExecutor(locationCommand);
        Objects.requireNonNull(getCommand("location")).setTabCompleter(locationCommand);

        final CoreCommand coreCommand = new CoreCommand(pluginConfig.getMessanger(), pluginConfig);
        Objects.requireNonNull(getCommand("crowncore")).setExecutor(coreCommand);
        Objects.requireNonNull(getCommand("crowncore")).setTabCompleter(coreCommand);
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
        pluginManager.registerEvents(new PlayerQuit(playerDataService), this);
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

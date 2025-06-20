package de.obey.crown.core;

import de.obey.crown.core.command.CoreCommand;
import de.obey.crown.core.command.LocationCommand;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.event.CoreStartEvent;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.listener.CoreStart;
import de.obey.crown.core.listener.PlayerChat;
import de.obey.crown.core.listener.PlayerJoin;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.Teleporter;
import de.obey.crown.core.util.UUIDFetcher;
import de.obey.crown.core.util.VersionChecker;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public final class CrownCore extends JavaPlugin {

    private ExecutorService executorService;
    private VersionChecker versionChecker;

    private boolean placeholderapi = false;

    private PluginConfig pluginConfig;
    private Sounds sounds;

    @Override
    public void onLoad() {
        executorService = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("CrownCore-Worker-" + thread.getId());
            return thread;
        });

        pluginConfig = new PluginConfig(this);
        sounds = pluginConfig.getSounds();
    }

    @Override
    public void onEnable() {

        UUIDFetcher.initHTTPClient();
        versionChecker = new VersionChecker(executorService);
        versionChecker.retrieveNewestPluginVersions();

        // check if placeholderapi is present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderapi = true;
            new Placeholders().register();
        }

        Scheduler.initialize();

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

        pluginManager.registerEvents(new CoreStart(), this);
        pluginManager.registerEvents(new PlayerChat(pluginConfig, sounds), this);
        pluginManager.registerEvents(new PlayerJoin(pluginConfig, versionChecker), this);
    }

    public static CrownCore getInstance() {
        return getPlugin(CrownCore.class);
    }

    /*
 ______   _____
/\  ___\ /\  __-.
\ \ \____\ \ \/\ \
 \ \_____\\ \____-
  \/_____/ \/____/
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

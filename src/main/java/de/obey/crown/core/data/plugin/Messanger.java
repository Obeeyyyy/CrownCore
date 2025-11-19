/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.data.plugin;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.TextUtil;
import de.obey.crown.core.util.VaultHook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@RequiredArgsConstructor
@Getter
@Setter
public final class Messanger {

    private final CrownCore crownCore = CrownCore.getInstance();

    private final Plugin plugin;
    private final Sounds sounds;

    private String prefix, whiteColor, accentColor;
    private final Map<String, String> messages = Maps.newConcurrentMap();
    private final Map<String, ArrayList<String>> multiLineMessages = Maps.newConcurrentMap();

    public void load() {
        final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        loadCoreMessages();
        loadCorePlaceholders();
        loadPluginPlaceholders(configuration);

        checkForMissingMultiLineMessageEntries();
        checkForMissingMessageEntries();
    }

    private void loadPluginPlaceholders(final YamlConfiguration configuration) {
        prefix = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_prefix%", FileUtil.getString(configuration, "prefix", prefix));
        whiteColor = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_white%",FileUtil.getString(configuration, "white", whiteColor));
        accentColor = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_accent%",FileUtil.getString(configuration, "accent", accentColor));
    }

    private void loadCorePlaceholders() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(FileUtil.getCoreFile("messages.yml"));

        prefix = TextUtil.registerCorePlaceholder("%prefix%", FileUtil.getString(configuration, "prefix", "&5&lCORE &8●&f"));
        whiteColor = TextUtil.registerCorePlaceholder("%white%", FileUtil.getString(configuration, "white", "&f"));
        accentColor = TextUtil.registerCorePlaceholder("%accent%", FileUtil.getString(configuration, "accent", "&5"));
    }

    private void loadCoreMessages() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(FileUtil.getCoreFile("messages.yml"));

        if (!configuration.contains("messages"))
            return;

        for (final String key : configuration.getConfigurationSection("messages").getKeys(false)) {
            final String value = configuration.getString("messages." + key);
            messages.put(key, value);
        }
    }

    private void loadMessages(final YamlConfiguration configuration) {
        if (configuration.contains("messages")) {
            for (final String key : configuration.getConfigurationSection("messages").getKeys(false)) {
                final String value = configuration.getString("messages." + key);
                messages.put(key, value);
            }
        }
    }

    private void loadMultiLineMessages(final YamlConfiguration configuration) {
        if (!configuration.contains("multi-line-messages"))
            return;

        for (final String key : configuration.getConfigurationSection("multi-line-messages").getKeys(false)) {
            multiLineMessages.put(key, (ArrayList<String>) FileUtil.getStringArrayList(configuration, "multi-line-messages." + key, new ArrayList()));
        }
    }

    private void checkForMissingMessageEntries() {
        crownCore.getExecutor().submit(() -> {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null) {
                    defaults.load(new InputStreamReader(stream));
                }

                if (!defaults.contains("messages")) {
                    return;
                }

                final Set<String> messageKeys = defaults.getConfigurationSection("messages").getKeys(false);

                if (messageKeys.isEmpty()) {
                    return;
                }

                for (final String messageKey : messageKeys) {
                    if(configuration.contains("messages." + messageKey)) {
                        continue;
                    }

                    CrownCore.log.info("generated missing message key '" + messageKey + "' for plugin " + plugin.getName());

                    configuration.set("messages." + messageKey, defaults.getString("messages." + messageKey));
                }

                loadMessages(configuration);
                FileUtil.saveConfigurationIntoFile(configuration, file);

            } catch (final IOException | InvalidConfigurationException ignored) {}
        });
    }

    private void checkForMissingMultiLineMessageEntries() {
        crownCore.getExecutor().submit(() -> {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null) {
                    defaults.load(new InputStreamReader(stream));
                }

                if (!defaults.contains("multi-line-messages")) {
                    return;
                }

                final Set<String> messageKeys = defaults.getConfigurationSection("multi-line-messages").getKeys(false);

                if (messageKeys.isEmpty()) {
                    return;
                }

                for (final String messageKey : messageKeys) {
                    if(configuration.contains("multi-line-messages." + messageKey)) {
                        continue;
                    }

                    CrownCore.log.info("generated missing multi-line-message key '" + messageKey + "' for plugin " + plugin.getName());

                    configuration.set("multi-line-messages." + messageKey, defaults.getStringList("multi-line-messages." + messageKey));
                }

                loadMultiLineMessages(configuration);
                FileUtil.saveConfigurationIntoFile(configuration, file);
            } catch (final IOException | InvalidConfigurationException ignored) {}
        });
    }


    private void generateMessageEntryIfMissing(final String key) {
        if (!messages.containsKey(key)) {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null) {
                    defaults.load(new InputStreamReader(stream));
                }
            } catch (final IOException exception) {
                exception.printStackTrace();
            } catch (final InvalidConfigurationException exception) {
                throw new RuntimeException(exception);
            }

            if (defaults.contains("messages." + key)) {
                final String value = defaults.getString("messages." + key);
                configuration.set("messages." + key, value);
                messages.put(key, value);
            } else {
                configuration.set("messages." + key, "");
                messages.put(key, "");
            }

            FileUtil.saveConfigurationIntoFile(configuration, file);
        }
    }

    private void generateMultiLineMessageEntryIfMissing(final String key) {
        if (!multiLineMessages.containsKey(key)) {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            final YamlConfiguration defaults = new YamlConfiguration();
            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null) {
                    defaults.load(new InputStreamReader(stream));
                }
            } catch (final IOException exception) {
                exception.printStackTrace();
            } catch (final InvalidConfigurationException exception) {
                throw new RuntimeException(exception);
            }

            if (defaults.contains("multi-line-messages." + key)) {
                final List<String> value = defaults.getStringList("multi-line-messages." + key);
                configuration.set("multi-line-messages." + key, value);
                multiLineMessages.put(key, new ArrayList<>(value));
            } else {
                configuration.set("multi-line-messages." + key, new ArrayList<>());
                multiLineMessages.put(key, new ArrayList<>());
            }

            FileUtil.saveConfigurationIntoFile(configuration, file);
        }
    }

    private void sendLineToEveryPlayer(final String line) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        for (final Player all : Bukkit.getOnlinePlayers()) {
            sendNonConfigMessage(all, line);
        }
    }

    /* normal, multiline and raw messages */

    public String getMessage(final String key) {
        return getMessage(key, null);
    }

    public String getMessage(final String key, final String[] placeholders, final String... replacements) {
        return getMessageWithPlaceholderAPI(null, key, placeholders, replacements);
    }

    public String getRawMessageWithPlacehodlerAPI(final String key) {
        return getRawMessageWithPlacehodlerAPI(null, key);
    }

    public String getRawMessageWithPlacehodlerAPI(final OfflinePlayer offlinePlayer, final String key) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, getRawMessage(key));
    }

    public String getRawMessageWithPlacehodlerAPI(final OfflinePlayer offlinePlayer, final String key, final String[] placeholders, final String... replacements) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, getRawMessage(key, placeholders, replacements));
    }

    public String getRawMessageWithPlacehodlerAPI(final String key, final String[] placeholders, final String... replacements) {
        return PlaceholderAPI.setPlaceholders(null, getRawMessage(key, placeholders, replacements));
    }

    public String getRawMessage(final String key) {
        return getRawMessage(key, null);
    }

    public String getRawMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMessageEntryIfMissing(key);

        if (messages.get(key).equalsIgnoreCase("")) {
            return "";
        }

        String message = messages.get(key);

        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                message = message.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        return TextUtil.translateCorePlaceholderRaw(message);
    }

    public String getMessageWithPlaceholderAPI(final OfflinePlayer player, final String key) {
        if (player == null) {
            return getMessageWithPlaceholderAPI(null, key, null);
        }

        return getMessageWithPlaceholderAPI(player.getPlayer(), key, null);
    }


    public String getMessageWithPlaceholderAPI(final OfflinePlayer player, final String key, final String[] placeholders, final String... replacements) {
        generateMessageEntryIfMissing(key);

        if (messages.get(key).equalsIgnoreCase("")) {
            return "";
        }

        String message = messages.get(key);

        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                message = message.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        return TextUtil.translateColors(PlaceholderAPI.setPlaceholders(player, message));
    }

    public ArrayList<String> getMultiLineMessage(final String key) {
        return getMultiLineMessage(key, null);
    }

    public ArrayList<String> getMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMultiLineMessageEntryIfMissing(key);

        final ArrayList<String> lines = multiLineMessages.get(key);
        final ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            if (placeholders != null) {
                int count = 0;
                for (final String placeholder : placeholders) {
                    line = line.replace("%" + placeholder + "%", replacements[count]);
                    count++;
                }
            }

            line = TextUtil.translateColors(line);
            temp.add(line);
        }

        return temp;
    }

    public ArrayList<String> getRawMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMultiLineMessageEntryIfMissing(key);

        final ArrayList<String> lines = multiLineMessages.get(key);
        final ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            if (placeholders != null) {
                int count = 0;
                for (final String placeholder : placeholders) {
                    line = line.replace("%" + placeholder + "%", replacements[count]);
                    count++;
                }
            }

            line = TextUtil.translateCorePlaceholderRaw(line);
            temp.add(line);
        }

        return temp;
    }

    /*                           */
    /*     sending messages      */
    /*                           */

    public void sendNonConfigMessage(final CommandSender sender, final String message) {
        String line = message;

        if (sender instanceof Player player) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        sender.sendMessage(TextUtil.translateColors(line));
    }

    public void sendMessage(final CommandSender sender, final String key) {
        sendMessage(sender, key, null);
    }

    public void sendMessage(final CommandSender sender, final String key, final String[] placeholders,
                            final String... replacements) {

        final String message = getMessage(key, placeholders, replacements);

        if (message.isEmpty())
            return;

        sender.sendMessage(message);
    }

    public void sendMultiLineMessage(final CommandSender sender, final String key) {
        sendMultiLineMessage(sender, key, null);
    }

    public void sendMultiLineMessage(final CommandSender sender, final String key, final String[] placeholders, final String... replacements) {
        final ArrayList<String> lines = getMultiLineMessage(key, placeholders, replacements);
        if (lines.isEmpty())
            return;

        final ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            String tempLine = line;
            if (placeholders != null) {
                int count = 0;
                for (final String placeholder : placeholders) {
                    tempLine = tempLine.replace("%" + placeholder + "%", replacements[count]);
                    count++;
                }
            }

            temp.add(tempLine);
        }


        for (final String translatedLine : temp) {
            sender.sendMessage(PlaceholderAPI.setPlaceholders(null, translatedLine));
        }
    }

    /*               */
    /*  Broadcasts   */
    /*               */

    public void broadcastMessage(final String key) {
        broadcastMessage(key, null);
    }

    public void broadcastMessage(final String key, final String[] placeholders, final String... replacements) {
        broadcastMessagewithPlaceholderAPI(null, key, placeholders, replacements);
    }

    public void broadcastMessagewithPlaceholderAPI(final Player player, final String key, final String[] placeholders, final String... replacements) {

        String message = getMessageWithPlaceholderAPI(player, key);
        if (message.isEmpty())
            return;

        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                message = message.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        sendLineToEveryPlayer(message);
    }

    public void broadcastMultiLineMessage(final String key) {
        broadcastMultiLineMessage(key, null);
    }

    public void broadcastMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        final ArrayList<String> lines = getMultiLineMessage(key);
        if (lines.isEmpty())
            return;

        final ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            if (placeholders != null) {
                int count = 0;
                for (final String placeholder : placeholders) {
                    line = line.replace("%" + placeholder + "%", replacements[count]);
                    count++;
                }
            }

            temp.add(line);
        }


        for (final String translatedLine : temp) {
            sendLineToEveryPlayer(PlaceholderAPI.setPlaceholders(null, translatedLine));
        }
    }

    /*               */
    /*  Actionbar    */
    /*               */

    public void sendActionbar(final CommandSender sender, final String key) {
        sendActionbar(sender, key, null);
    }

    public void sendActionbar(final CommandSender sender, final String key, final String[] placeholders, final String... replacements) {
        final TextComponent textComponent = TextUtil.translateComponent(getRawMessage(key, placeholders, replacements));
        sender.sendActionBar(textComponent);
    }

    /*               */
    /*     Title     */
    /*               */

    public void sendTitle(final CommandSender sender, final Component title, final Component subTitle) {
        final Title obj = Title.title(title, subTitle);
        sender.showTitle(obj);
    }

    /*                    */
    /*  Text Components   */
    /*                    */

    public Component getComonentNonFile(final String message) {
        return TextUtil.translateComponent(TextUtil.translateCorePlaceholderRaw(message));
    }

    public Component getComonent(final String key) {
        return getRawComponent(key, null);
    }

    public Component getRawComponent(final String key, final String[] placeholders, final String... replacements) {
        return TextUtil.translateComponent(getRawMessage(key, placeholders, replacements));
    }

    public Component getComponent(final String key, final String[] placeholders, final String... replacements) {
        return TextUtil.translateComponent(getMessage(key, placeholders, replacements));
    }

    public void sendHoverableMessage(final CommandSender sender, Component component, final String hoverKey, final String[] placeholders, final String... replacements) {
        component = component.hoverEvent(HoverEvent.showText(getRawComponent(hoverKey, placeholders, replacements)));
        sender.sendMessage(component);
    }

    public void sendClickableMessage(final CommandSender sender, final String command, final String key) {
        sendClickableMessage(sender, command, key, null);
    }

    public void sendClickableMessage(final CommandSender sender, final String command, final String key, final String[] placeholders, final String... replacements) {
        sendClickableMessageWithHoverOption(sender, "&8 » &f" + command, command, key, placeholders, replacements);
    }

    public void sendClickableMessageWithHoverOption(final CommandSender sender,final String hoverOption, final String command, final String key) {
        final Component component = getRawComponent(key, null, "")
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .hoverEvent(HoverEvent.showText(TextUtil.translateComponent(hoverOption)));
        sender.sendMessage(component);
    }

    public void sendClickableMessageWithHoverOption(final CommandSender sender,final String hoverOption, final String command, final String key, final String[] placeholders, final String... replacements) {
        final Component component = getRawComponent(key, placeholders, replacements)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .hoverEvent(HoverEvent.showText(TextUtil.translateComponent(hoverOption)));
        sender.sendMessage(component);
    }

    /*               */
    /*  Methods      */
    /*               */


    public void sendCommandSyntax(final CommandSender sender, final String command, final String... lines) {
        if (!messages.containsKey("command-syntax"))
            return;

        final String syntaxPrefix = getMessage("command-syntax-prefix");

        sender.sendMessage("");
        sendMessage(sender, "command-syntax", new String[]{"command"}, command);
        for (final String line : lines) {
            sender.sendMessage(syntaxPrefix + line);
        }
    }

    public boolean hasEnoughMoney(final Player player, final double amount) {
        if (VaultHook.has(player, amount))
            return true;

        sendMessage(player, "not-enough-money", new String[]{"missing"}, TextUtil.formatNumber(amount - VaultHook.get(player)));

        return false;
    }

    public boolean hasPermission(final CommandSender sender, final String permission) {
        return hasPermission(sender, permission, true);
    }

    public boolean hasPermission(final CommandSender sender, final String permission, final boolean send) {
        if (sender.hasPermission(permission))
            return true;

        if (send) {
            if (sender instanceof Player player)
                sounds.playSoundToPlayer(player, "no-permission");

            sendMessage(sender, "no-permission", new String[]{"permission"}, permission);
        }

        return false;
    }

    public boolean isValidPlayerName(final CommandSender sender, final String name) {
        if (!name.matches("[a-zA-z0-9]{3,16}")) {

            if (sender instanceof Player player)
                sounds.playSoundToPlayer(player, "player-invalid");

            sendMessage(sender, "player-invalid", new String[]{"name"}, name);
            return false;
        }

        return true;
    }

    public boolean isKnown(final CommandSender sender, final String name) {
        if (!isValidPlayerName(sender, name))
            return false;

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (offlinePlayer.isOnline())
            return true;

        if (!offlinePlayer.hasPlayedBefore()) {

            if (sender instanceof Player player)
                sounds.playSoundToPlayer(player, "player-invalid");

            sendMessage(sender, "player-invalid", new String[]{"name"}, name);
            return false;
        }

        return true;
    }

    public boolean isOnline(final CommandSender sender, final String name) {

        if (!isValidPlayerName(sender, name))
            return false;

        final Player target = Bukkit.getPlayer(name);
        if (target == null || !target.isOnline()) {

            if (sender instanceof Player player)
                sounds.playSoundToPlayer(player, "player-offline");

            sendMessage(sender, "player-offline", new String[]{"name"}, name);
            return false;
        }

        return true;
    }

    public int isValidInt(final String input) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException exception) {
            return -9999;
        }
    }

    public int isValidInt(final CommandSender sender, final String input, final int min) {
        final int number = isValidInt(input);

        if (number < min) {
            sendMessage(sender, "invalid-number", new String[]{"min"}, TextUtil.formatNumber(min));
        }

        return number;
    }

    public long isValidLong(final String input) {
        try {
            return Long.parseLong(input);
        } catch (final NumberFormatException exception) {
            return -9999;
        }
    }

    public long isValidLong(final CommandSender sender, final String input, final long min) {
        final long number = isValidLong(input);

        if (number < min) {
            sendMessage(sender, "invalid-number", new String[]{"min"}, TextUtil.formatNumber(min));
        }

        return number;
    }


    public double isValidDouble(final String input) {
        try {
            return Double.parseDouble(input);
        } catch (final NumberFormatException exception) {
            return -9999;
        }
    }

    public double isValidDouble(final CommandSender sender, final String input, final double min) {
        final double number = isValidDouble(input);

        if (number < min) {
            sendMessage(sender, "invalid-number", new String[]{"min"}, TextUtil.formatNumber(min));
        }

        return number;
    }

}

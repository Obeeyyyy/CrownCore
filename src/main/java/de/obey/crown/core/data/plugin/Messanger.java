/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.data.plugin;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.FloodgateUtil;
import de.obey.crown.core.util.TextUtil;
import de.obey.crown.core.util.VaultHook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
@Setter
public final class Messanger {

    private final CrownCore crownCore = CrownCore.getInstance();

    private final Plugin plugin;
    private final Sounds sounds;

    private String prefix, whiteColor, accentColor;

    private final Map<String, String> legacyMessages = Maps.newConcurrentMap();
    private final Map<String, List<String>> legacyMultiLineMessages = Maps.newConcurrentMap();

    private final Map<String, String> messages = Maps.newConcurrentMap();
    private final Map<String, List<String>> multiLineMessages = Maps.newConcurrentMap();

    public void load() {
        final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        loadCoreMessages();
        loadCorePlaceholders();
        loadPluginPlaceholders(configuration);

        //checkForMissingMultiLineMessageEntries();
        checkForMissingMessageEntries();
    }

    /***
     * initializing the plugins placeholders from messages.yml.
     */
    private void loadPluginPlaceholders(final YamlConfiguration configuration) {
        prefix = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_prefix%", FileUtil.getString(configuration, "prefix", prefix));
        whiteColor = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_white%", FileUtil.getString(configuration, "white", whiteColor));
        accentColor = TextUtil.registerCorePlaceholder("%" + plugin.getName().toLowerCase() + "_accent%", FileUtil.getString(configuration, "accent", accentColor));
    }

    /***
     * initializing the core placeholders from CrownCore/messages.yml.
     */
    private void loadCorePlaceholders() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(FileUtil.getCoreFile("messages.yml"));

        prefix = TextUtil.registerCorePlaceholder("%prefix%", FileUtil.getString(configuration, "prefix", "&6&lCROWN &8●&f"));
        whiteColor = TextUtil.registerCorePlaceholder("%white%", FileUtil.getString(configuration, "white", "&f"));
        accentColor = TextUtil.registerCorePlaceholder("%accent%", FileUtil.getString(configuration, "accent", "&5"));
    }

    /***
     * loading messages from CrownCore/messsages.yml
     */
    private void loadCoreMessages() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(FileUtil.getCoreFile("messages.yml"));

        CrownCore.log.debug("[Messenger] <" + plugin.getName() + "> loading core messages");

        if (!configuration.contains("messages"))
            return;

        final ConfigurationSection section = configuration.getConfigurationSection("messages");

        if(section == null)
            return;

        for (final String key : section.getKeys(false)) {
            final Object value = configuration.get("messages." + key);

            if(value == null)
                continue;

            if(value instanceof String valueString) {
                CrownCore.log.debug("[Messenger] <" + plugin.getName() + "> loading core message " + key + " as string");

                legacyMessages.put(key, valueString);
                messages.put(key, TextUtil.convertLegacyToMiniMessage(valueString));
                continue;
            }

            if(value instanceof List<?> valueList) {
                CrownCore.log.debug("[Messenger] <" + plugin.getName() + "> loading core message " + key + " as list");

                final List<String> stringArrayList = (List<String>) valueList;
                final List<String> tmp = new ArrayList<>();

                legacyMultiLineMessages.put(key, stringArrayList);

                for (final String line : stringArrayList)
                    tmp.add(TextUtil.convertLegacyToMiniMessage(line));

                multiLineMessages.put(key, tmp);
            }
        }
    }

    /***
     * loading messages from plugin messages.yml
     * @param configuration YamlConfiguration of messages.yml
     */
    private void loadMessages(final File file, final YamlConfiguration configuration) {
        // move all multi line messages into messages

        if(configuration.contains("multi-line-messages")) {
            backupMessageFile(file);

            CrownCore.log.debug("[Messenger] <" + plugin.getName() + " found old multi-line-messages");
            CrownCore.log.debug("[Messenger] <" + plugin.getName() + " migrating entries into messages");
            final ConfigurationSection section = configuration.getConfigurationSection("multi-line-messages");

            if(section != null) {
                for (final String key : section.getKeys(false)) {
                    configuration.set("messages." + key, configuration.getStringList("multi-line-messages." + key));
                    CrownCore.log.debug("[Messenger] <" + plugin.getName() + " migrating entry: " + key);
                }
            }

            try {
                configuration.set("multi-line-messages", null);
                configuration.save(FileUtil.getGeneratedFile(plugin, "messages.yml", true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (configuration.contains("messages")) {
            final ConfigurationSection section = configuration.getConfigurationSection("messages");

            if(section == null)
                return;

            CrownCore.log.debug("[Messenger] <" + plugin.getName() + " loading messages");

            for (final String key : section.getKeys(false)) {
                final Object value = configuration.get("messages." + key);

                if(value == null) {
                    CrownCore.log.debug("[Messenger] <" + plugin.getName() + " value for key is null: " + key);
                    continue;
                }

                if(value instanceof String valueString) {
                    CrownCore.log.debug("[Messenger] <" + plugin.getName() + "> loading " + key + " as string");

                    legacyMessages.put(key, valueString);
                    messages.put(key, TextUtil.convertLegacyToMiniMessage(valueString));
                    continue;
                }

                if(value instanceof List<?> valueList) {
                    CrownCore.log.debug("[Messenger] <" + plugin.getName() + "> loading " + key + " as list");

                    final List<String> stringArrayList = (List<String>) valueList;
                    final List<String> tmp = new ArrayList<>();

                    legacyMultiLineMessages.put(key, stringArrayList);

                    for (final String line : stringArrayList)
                        tmp.add(TextUtil.convertLegacyToMiniMessage(line));

                    multiLineMessages.put(key, tmp);
                }
            }
        }
    }

    private void backupMessageFile(final File original) {
        if (!original.exists())
            return;

        final File backupFolder = new File(original.getParentFile(), "config-backups");

        if (!backupFolder.exists())
            backupFolder.mkdirs();

        final String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        final File backupFile = new File(backupFolder, original.getName().replace(".yml", "") + "-" + timestamp + ".yml");

        try {
            Files.copy(original.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            CrownCore.log.info("(" + plugin.getName() + ") created message backup: " + backupFile.getName());

        } catch (IOException exception) {
            CrownCore.log.warn("(" + plugin.getName() + ") failed to create message backup");
            exception.printStackTrace();
        }
    }

    private void loadMultiLineMessages(final YamlConfiguration configuration) {
        if (!configuration.contains("multi-line-messages"))
            return;

        final ConfigurationSection section = configuration.getConfigurationSection("multi-line-messages");

        if(section == null)
            return;

        for (final String key : section.getKeys(false)) {
            final List<String> stringArrayList = FileUtil.getStringArrayList(configuration, "multi-line-messages." + key, new ArrayList<>());
            final List<String> tmp = new ArrayList<>();

            legacyMultiLineMessages.put(key, stringArrayList);

            for (final String line : stringArrayList)
                tmp.add(TextUtil.convertLegacyToMiniMessage(line));

            multiLineMessages.put(key, tmp);
        }
    }

    private void checkForMissingMessageEntries() {
        CrownCore.log.debug("[Messenger] <" + plugin.getName() + " checking for missing entries");
        crownCore.getExecutor().execute(() -> {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null)
                    defaults.load(new InputStreamReader(stream));

                if (!defaults.contains("messages"))
                    return;

                final ConfigurationSection section = defaults.getConfigurationSection("messages");

                if(section == null)
                    return;

                final Set<String> messageKeys = section.getKeys(false);

                if (messageKeys.isEmpty())
                    return;

                for (final String messageKey : messageKeys) {
                    if (configuration.contains("messages." + messageKey))
                        continue;

                    CrownCore.log.info("[Messenger] <" + plugin.getName() + " generated missing entry: " + messageKey);
                    final Object defaultobj = defaults.get("messages." + messageKey);
                    configuration.set("messages." + messageKey, defaultobj);
                }

                loadMessages(file, configuration);
                FileUtil.saveConfigurationIntoFile(configuration, file);

            } catch (final IOException | InvalidConfigurationException ignored) {}
        });
    }

    private void checkForMissingMultiLineMessageEntries() {
        crownCore.getExecutor().execute(() -> {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null)
                    defaults.load(new InputStreamReader(stream));

                if (!defaults.contains("multi-line-messages"))
                    return;

                final ConfigurationSection section = defaults.getConfigurationSection("multi-line-messages");

                if(section == null)
                    return;

                final Set<String> messageKeys = section.getKeys(false);

                if (messageKeys.isEmpty())
                    return;

                for (final String messageKey : messageKeys) {
                    if (configuration.contains("multi-line-messages." + messageKey))
                        continue;

                    CrownCore.log.info("generated missing multi-line-message key '" + messageKey + "' for plugin " + plugin.getName());

                    configuration.set("multi-line-messages." + messageKey, defaults.getStringList("multi-line-messages." + messageKey));
                }

                //loadMultiLineMessages(configuration);
                FileUtil.saveConfigurationIntoFile(configuration, file);
            } catch (final IOException | InvalidConfigurationException ignored) {}
        });
    }


    private void generateMessageEntryIfMissing(final String key) {
        if (!legacyMessages.containsKey(key) || !messages.containsKey(key)) {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            final YamlConfiguration defaults = new YamlConfiguration();

            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null)
                    defaults.load(new InputStreamReader(stream));
            } catch (final IOException | InvalidConfigurationException exception) {
                throw new RuntimeException(exception);
            }

            if (defaults.contains("messages." + key)) {
                final String value = defaults.getString("messages." + key);
                configuration.set("messages." + key, value);
                legacyMessages.put(key, value);
                messages.put(key, TextUtil.convertLegacyToMiniMessage(value));
            } else {
                configuration.set("messages." + key, "");
                legacyMessages.put(key, "");
                messages.put(key, "");
            }

            FileUtil.saveConfigurationIntoFile(configuration, file);
        }
    }

    private void generateMultiLineMessageEntryIfMissing(final String key) {
        if (!legacyMultiLineMessages.containsKey(key) || !multiLineMessages.containsKey(key)) {
            final File file = FileUtil.getGeneratedFile(plugin, "messages.yml", true);
            final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            final YamlConfiguration defaults = new YamlConfiguration();
            try (final InputStream stream = plugin.getResource("messages.yml")) {
                if (stream != null)
                    defaults.load(new InputStreamReader(stream));
            } catch (final IOException | InvalidConfigurationException exception) {
                throw new RuntimeException(exception);
            }

            if (defaults.contains("messages." + key)) {
                final List<String> value = defaults.getStringList("messages." + key);
                configuration.set("messages." + key, value);

                legacyMultiLineMessages.put(key, new ArrayList<>(value));
                final List<String> tmp = new ArrayList<>();


                for (final String line : value)
                    tmp.add(TextUtil.convertLegacyToMiniMessage(line));

                multiLineMessages.put(key, tmp);
            } else {
                configuration.set("messages." + key, new ArrayList<>());
                legacyMultiLineMessages.put(key, new ArrayList<>());
                multiLineMessages.put(key, new ArrayList<>());
            }

            FileUtil.saveConfigurationIntoFile(configuration, file);
        }
    }

    private void sendLineToEveryPlayer(final String line) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        for (final Player all : Bukkit.getOnlinePlayers())
            sendNonConfigMessage(all, line);
    }

    private void sendLineToEveryPlayer(final Component component) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        for (final Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage(component);
        }
    }

    /* getMessageComponent methods*/
    public Component getMessageComponent(final String key) {
        return getMessageComponent(key, null);
    }

    public Component getMessageComponent(final String key, final String[] placeholders, final String... replacements) {
        return getMessageComponentWithPlaceholderAPI(null, key, placeholders, replacements);
    }

    public Component getMessageComponentWithPlaceholderAPI(final OfflinePlayer player, final String key, final String[] placeholders, final String... replacements) {
        generateMessageEntryIfMissing(key);

        if(!messages.containsKey(key))
            return Component.empty();

        if (messages.get(key).equalsIgnoreCase(""))
            return Component.empty();

        String message = messages.get(key);

        // replace internal placeholders
        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                message = message.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        final Pattern pattern = Pattern.compile("%([^%]+)%");

        // look for papi placeholders
        int maxIterations = 2;

        for (int i = 0; i < maxIterations; i++) {
            Matcher matcher = pattern.matcher(message);

            boolean changed = false;
            final StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String placeholder = matcher.group();

                String value = TextUtil.convertLegacyToMiniMessage(PlaceholderAPI.setPlaceholders(player, placeholder));

                if (!value.equals(placeholder)) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(value));
                    changed = true;
                } else {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(placeholder));
                }
            }

            matcher.appendTail(result);
            String newMessage = result.toString();

            if (!changed || newMessage.equals(message)) {
                message = newMessage;
                break;
            }

            message = newMessage;

        }

        return MiniMessage.miniMessage().deserialize(message);
    }

    /* normal, multiline and raw messages */
    public String getMessage(final String key) {
        return getMessage(key, null);
    }

    public String getMessage(final String key, final String[] placeholders, final String... replacements) {
        return getMessageWithPlaceholderAPI(null, key, placeholders, replacements);
    }

    public String getRawMessageWithPlaceholderAPI(final String key) {
        return getRawMessageWithPlaceholderAPI(null, key);
    }

    public String getRawMessageWithPlaceholderAPI(final OfflinePlayer offlinePlayer, final String key) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, getRawMessage(key));
    }

    public String getRawMessageWithPlaceholderAPI(final OfflinePlayer offlinePlayer, final String key, final String[] placeholders, final String... replacements) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, getRawMessage(key, placeholders, replacements));
    }

    public String getRawMessageWithPlaceholderAPI(final String key, final String[] placeholders, final String... replacements) {
        return PlaceholderAPI.setPlaceholders(null, getRawMessage(key, placeholders, replacements));
    }

    public String getRawMessage(final String key) {
        return getRawMessage(key, null);
    }

    public String getRawMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMessageEntryIfMissing(key);

        if (legacyMessages.get(key).equalsIgnoreCase(""))
            return "";

        String message = legacyMessages.get(key);

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
        if (player == null)
            return getMessageWithPlaceholderAPI(null, key, null);

        return getMessageWithPlaceholderAPI(player.getPlayer(), key, null);
    }


    public String getMessageWithPlaceholderAPI(final OfflinePlayer player, final String key, final String[] placeholders, final String... replacements) {
        generateMessageEntryIfMissing(key);

        if (messages.get(key).equalsIgnoreCase(""))
            return "";

        String message = messages.get(key);

        // replace internal placeholders
        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                message = message.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        final Pattern pattern = Pattern.compile("%([^%]+)%");

        // look for papi placeholders and check for mini message
        int maxIterations = 2;

        for (int i = 0; i < maxIterations; i++) {
            Matcher matcher = pattern.matcher(message);

            boolean changed = false;
            final StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String placeholder = matcher.group();

                String value = PlaceholderAPI.setPlaceholders(player, placeholder);
                value = TextUtil.convertLegacyToMiniMessage(value);

                if (!value.equals(placeholder)) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(value));
                    changed = true;
                } else {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(placeholder));
                }
            }

            matcher.appendTail(result);
            String newMessage = result.toString();

            if (!changed || newMessage.equals(message)) {
                message = newMessage;
                break;
            }

            message = newMessage;

        }

        return message;
    }

    public List<String> getMultiLineMessage(final String key) {
        return getMultiLineMessage(key, null);
    }

    public List<String> getMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMultiLineMessageEntryIfMissing(key);

        final List<String> lines = multiLineMessages.get(key);
        final List<String> temp = new ArrayList<>();

        if(lines == null)
            return temp;

        for (String line : lines) {
            if (placeholders != null) {
                int count = 0;
                for (final String placeholder : placeholders) {
                    line = line.replace("%" + placeholder + "%", replacements[count]);
                    count++;
                }
            }

            line = PlaceholderAPI.setPlaceholders(null, line);
            temp.add(line);
        }

        return temp;
    }

    public List<String> getRawMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        generateMultiLineMessageEntryIfMissing(key);

        final List<String> lines = multiLineMessages.get(key);
        final List<String> temp = new ArrayList<>();

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
        String line = TextUtil.convertLegacyToMiniMessage(message);

        if (sender instanceof Player player)
            line = PlaceholderAPI.setPlaceholders(player, line);

        //sender.sendMessage(TextUtil.translateColors(line));
        sender.sendMessage(MiniMessage.miniMessage().deserialize(line));
    }

    public void sendNonConfigMessage(final CommandSender sender, final String[] placeholders, final String message, final String... replacements) {
        String line = TextUtil.convertLegacyToMiniMessage(message);

        if (placeholders != null) {
            int count = 0;
            for (final String placeholder : placeholders) {
                line = line.replace("%" + placeholder + "%", replacements[count]);
                count++;
            }
        }

        if (sender instanceof Player player) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        //sender.sendMessage(TextUtil.translateColors(line));
        sender.sendMessage(MiniMessage.miniMessage().deserialize(line));
    }

    public void sendMessage(final CommandSender sender, final String key) {
        sendMessage(sender, key, null);
    }

    public void sendMessage(final CommandSender sender, final String key, final String[] placeholders,
                            final String... replacements) {

        final Component component = getMessageComponent(key, placeholders, replacements);

        if(component == Component.empty())
            return;

        sender.sendMessage(component);
    }

    public void sendNonConfigMultiLineMessage(final CommandSender sender, final List<String> lines) {
        sendNonConfigMultiLineMessage(sender, null, lines);
    }

    public void sendNonConfigMultiLineMessage(final CommandSender sender, final String[] placeholders, final List<String> lines, final String... replacements) {
        if (lines.isEmpty())
            return;

        final ArrayList<String> temp = new ArrayList<>();

        for (final String line : lines) {
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
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TextUtil.convertLegacyToMiniMessage(PlaceholderAPI.setPlaceholders(sender instanceof Player ? (Player) sender : null, translatedLine))));
        }
    }

    public void sendMultiLineMessage(final CommandSender sender, final String key) {
        sendMultiLineMessage(sender, key, null);
    }

    public void sendMultiLineMessage(final CommandSender sender, final String key, final String[] placeholders, final String... replacements) {
        if (!multiLineMessages.containsKey(key) && messages.containsKey(key)) {
            CrownCore.log.debug("did not find multiline key but found in messages: " + key);
            sendMessage(sender, key, placeholders, replacements);
            return;
        }

        final List<String> lines = getMultiLineMessage(key, placeholders, replacements);
        if (lines.isEmpty())
            return;

        final List<String> temp = new ArrayList<>();

        for (final String line : lines) {
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
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TextUtil.convertLegacyToMiniMessage(PlaceholderAPI.setPlaceholders(null, translatedLine))));
        }
    }

    /*               */
    /*  Broadcasts   */
    /*               */

    public void broadcastMessage(final String key) {
        broadcastMessage(key, null);
    }

    public void broadcastMessage(final String key, final String[] placeholders, final String... replacements) {
        broadcastMessageWithPlaceholderAPI(null, key, placeholders, replacements);
    }

    public void broadcastMessageWithPlaceholderAPI(final Player player, final String key, final String[] placeholders, final String... replacements) {
        final Component message = getMessageComponentWithPlaceholderAPI(player, key, placeholders, replacements);

        sendLineToEveryPlayer(message);
    }

    public void broadcastMultiLineMessage(final String key) {
        broadcastMultiLineMessage(key, null);
    }

    public void broadcastMultiLineMessage(final String key, final String[] placeholders, final String... replacements) {
        final List<String> lines = getMultiLineMessage(key,  placeholders, replacements);
        if (lines.isEmpty())
            return;

        for (final String translatedLine : lines) {
            final String finalfinal = PlaceholderAPI.setPlaceholders(null, translatedLine);
            sendLineToEveryPlayer(finalfinal);
        }
    }

    /*               */
    /*  Actionbar    */
    /*               */

    public void sendActionbar(final CommandSender sender, final String key) {
        sendActionbar(sender, key, null);
    }

    public void sendActionbar(final CommandSender sender, final String key, final String[] placeholders, final String... replacements) {
        //final TextComponent textComponent = TextUtil.translateComponent(getRawMessage(key, placeholders, replacements));
        //sender.sendActionBar(MiniMessage.miniMessage().deserialize(getMessage(key, placeholders, replacements)));

        if(sender instanceof Player player)
            sender.sendActionBar(getMessageComponentWithPlaceholderAPI(player, key, placeholders, replacements));
    }

    public void sendActionbarToAll(final String key) {
        for (final Player all : Bukkit.getOnlinePlayers()) {
            sendActionbar(all, key, null);
        }
    }

    public void sendActionbarAll(final String key, final String[] placeholders, final String... replacements) {
        final Component textComponent = MiniMessage.miniMessage().deserialize(getMessage(key, placeholders, replacements));
        for (final Player all : Bukkit.getOnlinePlayers()) {
            all.sendActionBar(textComponent);
        }
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

    public Component getComponentNonFile(final String message) {
        return MiniMessage.miniMessage().deserialize(TextUtil.translateCorePlaceholderRaw(message));
    }

    public Component getComponent(final String key) {
        return MiniMessage.miniMessage().deserialize(getMessage(key, null));
    }

    public Component getRawComponent(final String key, final String[] placeholders, final String... replacements) {
        return MiniMessage.miniMessage().deserialize(getRawMessage(key, placeholders, replacements));
    }

    public Component getComponent(final String key, final String[] placeholders, final String... replacements) {
        return MiniMessage.miniMessage().deserialize(getMessage(key, placeholders, replacements));
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
            sender.sendMessage(MiniMessage.miniMessage().deserialize(syntaxPrefix + line));
        }
    }

    public boolean hasEnoughMoney(final Player player, final double amount) {
        if (VaultHook.has(player, amount))
            return true;

        sendMessage(player, "not-enough-money", new String[]{"missing"}, TextUtil.formatNumber(amount - VaultHook.get(player)));
        sounds.playSoundToPlayer(player, "not-enough-money");

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

    public CompletableFuture<Boolean> isBedrockPlayer(final CommandSender sender, final String name) {
        return FloodgateUtil.isBedrockPlayer(name).thenApply((state) -> {

            if (!state) {
                if (sender instanceof Player player)
                    sounds.playSoundToPlayer(player, "player-invalid");

                sendMessage(sender, "player-invalid", new String[]{"name"}, name);
            }

            return state;
        });
    }

    public boolean isValidPlayerName(final CommandSender sender, final String name) {
        final String bedrockPrefix = FloodgateUtil.getBedrockPrefix();
        final String escapedPrefix = Pattern.quote(bedrockPrefix);
        final String regex = "^(?:" + escapedPrefix + ")?[A-Za-z0-9_]{3,16}$";

        if (!name.matches(regex)) {

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

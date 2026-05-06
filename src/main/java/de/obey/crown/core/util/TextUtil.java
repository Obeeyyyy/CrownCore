/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public final class TextUtil {

    @Getter
    private final Map<String, String> placeholders = Maps.newConcurrentMap();
    private final Map<String, String> rawPlaceholders = Maps.newConcurrentMap();

    private final Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");
    private final Pattern HEX_PATTERN_TWO = Pattern.compile("&#[A-Fa-f0-9]{6}");
    private final Pattern HEX_COMBINED = Pattern.compile("&#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{6}");

    private final Pattern durationStringPattern = Pattern.compile("(\\d+)\\s*(d|h|m|s)");

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .character('&')
            .build();

    @Setter
    private DecimalFormat decimalFormat = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
    @Setter
    private boolean useShortFormat = false;

    public String reverse(final String text) {
        String value = "";
        for (int i = 0; i < text.length(); i++) {
            value = text.charAt(i) + value;
        }
        return value;
    }

    public long parseDurationStringToMillis(String input) {
        long totalMillis = 0;

        input = input.toLowerCase().trim();

        final Matcher matcher = durationStringPattern.matcher(input);

        while (matcher.find()) {
            final long value = Long.parseLong(matcher.group(1));
            final String unit = matcher.group(2);

            switch (unit) {
                case "d": totalMillis += value * 24L * 60L * 60L * 1000L; break;
                case "h": totalMillis += value * 60L * 60L * 1000L; break;
                case "m": totalMillis += value * 60L * 1000L; break;
                case "s": totalMillis += value * 1000L; break;
            }
        }

        return totalMillis;
    }


    public boolean containsOnlyLettersAndNumbers(final String input) {
        return input.matches("\\w+");
    }

    public String registerCorePlaceholder(final String placeholder, String replacement) {
        rawPlaceholders.put(placeholder, replacement);
        replacement = convertLegacyToMiniMessage(replacement);
        placeholders.put(placeholder, replacement);
        return replacement;
    }

    public String formatNumberShort(final double value) {
        double absValue = Math.abs(value);
        final String[] suffixes = {"", "k", "M", "B", "T", "Q"};
        int index = 0;

        while (absValue >= 1000 && index < suffixes.length - 1) {
            absValue /= 1000;
            index++;
        }

        String formatted = absValue % 1 == 0 ? String.format("%.0f", absValue) : String.format("%.1f", absValue);

        return formatted + suffixes[index];
    }

    public double getDoubleFromStringWithSuffix(String text) {
        text = text.toLowerCase();

        if (text.contains("k")) {
            try {
                String number = text
                        .replace("k", "")
                        .replace(",", ".");

                return Double.parseDouble(number) * 1000;
            } catch (final NumberFormatException exception) {
                return -1;
            }
        }

        if (text.contains("mil") || text.contains("m")) {
            try {
                String number = text
                        .replace("m", "")
                        .replace("mil", "")
                        .replace(",", ".");

                return Double.parseDouble(number) * 1_000_000;
            } catch (final NumberFormatException exception) {
                return -1;
            }
        }

        if (text.contains("bil") || text.contains("b")) {
            try {
                String number = text
                        .replace("bil", "")
                        .replace("b", "")
                        .replace(",", ".");

                return Double.parseDouble(number) * 1_000_000_000;
            } catch (final NumberFormatException exception) {
                return -1;
            }
        }

        return -1;
    }

    public Location parseStringToLocation(final String data) {
        // #world#x#y#z#yaw#pitch
        final String[] parts = data.split("#");
        final World world = Bukkit.getWorld(parts[1]);

        if (world == null) {
            return null;
        }

        return new Location(world,
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                Float.parseFloat(parts[5]),
                Float.parseFloat(parts[6]));
    }

    public String unixTimeStampToIso8601(long timestamp) {
        final Instant instant = Instant.ofEpochSecond(timestamp);
        return instant.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public String parseLocationToString(final Location location) {
        // #world#x#y#z#yaw#pitch
        return "#" + location.getWorld().getName() +
                "#" + location.getX() + "#" +
                location.getY() + "#" +
                location.getZ() + "#" +
                location.getYaw() + "#" +
                location.getPitch();
    }

    public String formatTimeStringWithFormat(long millis, final String format) {
        if (format == null || format.isEmpty())
            return "";

        long totalSeconds = millis / 1000;
        long milliseconds = millis % 1000;

        long days = 0, hours = 0, minutes = 0, seconds = 0;

        if (format.contains("dd") || format.contains("d")) {
            days = totalSeconds / 86400;
            totalSeconds %= 86400;
        }
        if (format.contains("hh") || format.contains("h")) {
            hours = totalSeconds / 3600;
            totalSeconds %= 3600;
        }
        if (format.contains("mm") || format.contains("m")) {
            minutes = totalSeconds / 60;
            totalSeconds %= 60;
        }

        seconds = totalSeconds;

        final long fd = days;
        final long fh = hours;
        final long fm = minutes;
        final long fs = seconds;

        String processed = processOptionalGroups(format, fd, fh, fm, fs, milliseconds);

        final String oneDecimal  = String.format(Locale.US, "%01d", milliseconds / 100);
        final String twoDecimals = String.format(Locale.US, "%02d", milliseconds / 10);

        String result = processed
                .replace("%dd%",  String.format(Locale.US, "%02d", days))
                .replace("%hh%",  String.format(Locale.US, "%02d", hours))
                .replace("%mm%",  String.format(Locale.US, "%02d", minutes))
                .replace("%ss%",  String.format(Locale.US, "%02d", seconds))
                .replace("%SSS%", String.format(Locale.US, "%03d", milliseconds))
                .replace("%tt%",  twoDecimals)
                .replace("%d%",   String.valueOf(days))
                .replace("%h%",   String.valueOf(hours))
                .replace("%m%",   String.valueOf(minutes))
                .replace("%s%",   String.valueOf(seconds))
                .replace("%t%",   oneDecimal)
                .replace("%S%",   String.valueOf(milliseconds));

        return result.replaceAll(" {2,}", " ").trim();
    }

    /**
     * process all (...) groups in the format string.
     * a group is removed if contains >=1 time
     */
    private String processOptionalGroups(String fmt, long days, long hours, long minutes, long seconds, long milliseconds) {

        final Pattern groupPattern = Pattern.compile("\\(([^()]+)\\)");
        final Matcher matcher = groupPattern.matcher(fmt);
        final StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String content = matcher.group(1);

            boolean hasToken = false;
            boolean allZero  = true;

            if (content.contains("%dd%") || content.contains("%d%")) {
                hasToken = true;
                if (days != 0) allZero = false;
            }
            if (content.contains("%hh%") || content.contains("%h%")) {
                hasToken = true;
                if (hours != 0) allZero = false;
            }
            if (content.contains("%mm%") || content.contains("%m%")) {
                hasToken = true;
                if (minutes != 0) allZero = false;
            }
            if (content.contains("%ss%") || content.contains("%s%")) {
                hasToken = true;
                if (seconds != 0) allZero = false;
            }
            if (content.contains("%SSS%") || content.contains("%tt%")
                    || content.contains("%t%")  || content.contains("%S%")) {
                hasToken = true;
                if (milliseconds != 0) allZero = false;
            }

            if (hasToken && allZero) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(content));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String formatTimeString(long millis) {
        return formatTimeStringWithFormat(millis, CrownCore.getInstance().getPluginConfig().getDefaultTimeFormat());
    }

    public String formatNumber(final long value) {

        if(useShortFormat)
            return formatNumberShort(value);

        return decimalFormat.format(value);
    }

    public String formatNumber(final double value) {

        if(useShortFormat)
            return formatNumberShort(value);

        return decimalFormat.format(value);
    }

    public String formatNumber(final long value, final String format) {
        final DecimalFormat temp = new DecimalFormat(format);
        return temp.format(value);
    }

    public String formatNumber(final double value, final String format) {
        final DecimalFormat temp = new DecimalFormat(format);
        return temp.format(value);
    }

    public String translateMiniMessage(String message ) {
        if (message == null)
            return "";

        return miniMessage.deserialize(message).toString();
    }

    public String translateHexColors(String message) {
        if (message == null)
            return "";

        Matcher matcherTWO = HEX_PATTERN_TWO.matcher(message);

        while (matcherTWO.find()) {
            final String code = message.substring(matcherTWO.start(), matcherTWO.end());
            message = message.replace(code, "" + ChatColor.of(code.substring(1)));
            matcherTWO = HEX_PATTERN_TWO.matcher(message);
        }

        Matcher matcher = HEX_PATTERN.matcher(message);

        while (matcher.find()) {
            final String code = message.substring(matcher.start(), matcher.end());
            message = message.replace(code, "" + ChatColor.of(code));
            matcher = HEX_PATTERN.matcher(message);
        }

        return message;
    }

    /*
    public net.kyori.adventure.text.TextComponent translateComponent(String message) {
        final Matcher matcher = HEX_COMBINED.matcher(message);
        net.kyori.adventure.text.TextComponent mainComponent = Component.empty();
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                mainComponent = mainComponent.append(Component.text().content(message.substring(lastIndex, matcher.start())));
            }

            String hexColor = matcher.group(0);
            final TextColor textColor = TextColor.fromCSSHexString(hexColor);

            int textStart = matcher.end();
            int nextColorStart = textStart;

            while (nextColorStart < message.length()) {
                Matcher nextMatcher = HEX_COMBINED.matcher(message.substring(nextColorStart));
                if (nextMatcher.lookingAt())
                    break;

                nextColorStart++;
            }

            final String coloredText = message.substring(textStart, nextColorStart);

            if (!coloredText.isEmpty()) {
                net.kyori.adventure.text.TextComponent colorComponent = Component.text().content(translateLegacyColors(coloredText)).build();
                colorComponent = colorComponent.color(textColor);
                mainComponent = mainComponent.append(colorComponent);
            }

            lastIndex = nextColorStart;
        }

        if (lastIndex < message.length()) {
            mainComponent = mainComponent.append(Component.text().content(message.substring(lastIndex)));
        }

        return mainComponent;
    }

     */

    public net.kyori.adventure.text.TextComponent translateComponent(String message) {
        final Matcher matcher = HEX_COMBINED.matcher(message);
        TextComponent mainComponent = Component.empty();
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                String preHexText = message.substring(lastIndex, matcher.start());
                mainComponent = mainComponent.append(Component.text(translateLegacyColors(preHexText)));
            }

            String hexColor = matcher.group();

            if(hexColor.startsWith("&")) {
                hexColor = hexColor.substring(1);
            }

            final TextColor textColor = TextColor.fromCSSHexString(hexColor);

            final int textStart = matcher.end();
            int nextColorStart = textStart;

            while (nextColorStart < message.length()) {
                final Matcher nextMatcher = HEX_COMBINED.matcher(message.substring(nextColorStart));
                if (nextMatcher.lookingAt()) break;
                nextColorStart++;
            }

            final String coloredText = message.substring(textStart, nextColorStart);
            if (!coloredText.isEmpty()) {
                final TextComponent colorComponent = Component.text(translateLegacyColors(coloredText)).color(textColor);
                mainComponent = mainComponent.append(colorComponent);
            }

            lastIndex = nextColorStart;
        }

        if (lastIndex < message.length()) {
            mainComponent = mainComponent.append(Component.text(translateLegacyColors(message.substring(lastIndex))));
        }

        return mainComponent.decoration(TextDecoration.ITALIC, false);
    }

    public String translateLegacyColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String translateCorePlaceholderRaw(String message) {
        if (message == null)
            return "";

        if (message.isEmpty())
            return message;

        if (rawPlaceholders.isEmpty())
            return message;

        for (final String key : rawPlaceholders.keySet()) {
            if (message.contains(key)) {
                message = message.replace(key, rawPlaceholders.get(key));
            }
        }

        return message;
    }

    public String translateCorePlaceholder(String message) {
        if (message == null)
            return "";

        if (message.isEmpty())
            return message;

        if (placeholders.isEmpty())
            return message;


        for (final String key : placeholders.keySet()) {
            if (message.contains(key)) {
                message = message.replace(key, placeholders.get(key));
            }
        }

        return message;
    }

    public String translatePlaceholders(final Player player, final String message) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    public String translateColors(final String message) {
        return translateLegacyColors(translateHexColors(translateCorePlaceholder(message)));
    }

    public String miniToLegacyIfMini(String input) {
        if (input == null || input.isEmpty()) return input;

        try {
            final Component component = MiniMessage.miniMessage().deserialize(input);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (final Exception ignored) {
            return input;
        }
    }

    private final Map<Character, String> LEGACY_TO_MINI = Maps.newConcurrentMap();

    static {
        LEGACY_TO_MINI.put('0', "<black>");
        LEGACY_TO_MINI.put('1', "<dark_blue>");
        LEGACY_TO_MINI.put('2', "<dark_green>");
        LEGACY_TO_MINI.put('3', "<dark_aqua>");
        LEGACY_TO_MINI.put('4', "<dark_red>");
        LEGACY_TO_MINI.put('5', "<dark_purple>");
        LEGACY_TO_MINI.put('6', "<gold>");
        LEGACY_TO_MINI.put('7', "<gray>");
        LEGACY_TO_MINI.put('8', "<dark_gray>");
        LEGACY_TO_MINI.put('9', "<blue>");
        LEGACY_TO_MINI.put('a', "<green>");
        LEGACY_TO_MINI.put('b', "<aqua>");
        LEGACY_TO_MINI.put('c', "<red>");
        LEGACY_TO_MINI.put('d', "<light_purple>");
        LEGACY_TO_MINI.put('e', "<yellow>");
        LEGACY_TO_MINI.put('f', "<white>");
        LEGACY_TO_MINI.put('k', "<obfuscated>");
        LEGACY_TO_MINI.put('l', "<bold>");
        LEGACY_TO_MINI.put('m', "<strikethrough>");
        LEGACY_TO_MINI.put('n', "<underlined>");
        LEGACY_TO_MINI.put('o', "<italic>");
        LEGACY_TO_MINI.put('r', "<reset>");
    }

    public String convertLegacyToMiniMessage(String input) {
        input = translateCorePlaceholder(input);
        final StringBuilder sb = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);

            // #ffffff hex outside of mimimessage
            if (c == '#' && i + 6 < input.length()) {
                final char prev = i > 0 ? input.charAt(i - 1) : 0;
                if (prev != '<' && prev != ':') {
                    final String hex = input.substring(i + 1, i + 7);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        sb.append('<').append('#').append(hex).append('>');
                        i += 6;
                        continue;
                    }
                }
            }

            if (c == '&' || c == '§') {
                // &#ffffff hex
                if (i + 7 < input.length() && input.charAt(i + 1) == '#') {
                    final String hex = input.substring(i + 2, i + 8);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        sb.append('<').append('#').append(hex).append('>');
                        i += 7;
                        continue;
                    }
                }

                // legacy codes
                if (i + 1 < input.length()) {
                    final char code = Character.toLowerCase(input.charAt(i + 1));
                    final String replacement = LEGACY_TO_MINI.get(code);

                    if (replacement != null) {
                        sb.append(replacement);
                        i++;
                        continue;
                    }
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private boolean isHexChar(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }



}

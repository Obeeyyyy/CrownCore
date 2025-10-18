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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

    @Setter
    private DecimalFormat decimalFormat = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));

    public String reverse(final String text) {
        String value = "";
        for (int i = 0; i < text.length(); i++) {
            value = text.charAt(i) + value;
        }
        return value;
    }

    public boolean containsOnlyLettersAndNumbers(final String input) {
        return input.matches("\\w+");
    }

    public String registerCorePlaceholder(final String placeholder, String replacement) {
        rawPlaceholders.put(placeholder, replacement);
        replacement = translateHexColors(translateLegacyColors(replacement));
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

        if (world == null)
            return null;

        return new Location(world,
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                Float.parseFloat(parts[5]),
                Float.parseFloat(parts[6]));
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
        if (format == null || format.isEmpty()) {
            return "";
        }

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

        final String oneDecimal = String.format(Locale.US, "%01d", milliseconds / 100);
        final String twoDecimals = String.format(Locale.US, "%02d", milliseconds / 10);

        return format
                .replace("dd", String.format("%02d", days))
                .replace("hh", String.format("%02d", hours))
                .replace("mm", String.format("%02d", minutes))
                .replace("ss", String.format("%02d", seconds))
                .replace("SSS", String.format("%03d", milliseconds))
                .replace("tt", twoDecimals)
                .replace("d", String.valueOf(days))
                .replace("h", String.valueOf(hours))
                .replace("m", String.valueOf(minutes))
                .replace("s", String.valueOf(seconds))
                .replace("t", oneDecimal)
                .replace("S", String.valueOf(milliseconds));
    }

    public String formatTimeString(long millis) {
        return formatTimeStringWithFormat(millis, CrownCore.getInstance().getPluginConfig().getDefaultTimeFormat());
    }

    public String formatNumber(final long value) {
        return decimalFormat.format(value);
    }

    public String formatNumber(final double value) {

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

            if(hexColor.startsWith("&"))
                hexColor = hexColor.substring(1);

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

        return mainComponent;
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
}

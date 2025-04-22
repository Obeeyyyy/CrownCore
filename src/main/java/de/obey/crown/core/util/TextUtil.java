/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
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
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));

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
        String edited = formatNumber((int) value);

        if (value <= 999)
            return edited;

        if (edited.length() == 5) {
            edited = edited.substring(0, 4) + "k";
        } else if (edited.length() == 6) {
            edited = edited.substring(0, 5) + "k";
        } else if (edited.length() == 7) {
            edited = edited.substring(0, 6) + "k";
        } else if (edited.length() == 9) {
            edited = edited.substring(0, 4) + "M";
        } else if (edited.length() == 10) {
            edited = edited.substring(0, 5) + "M";
        } else if (edited.length() == 11) {
            edited = edited.substring(0, 6) + "M";
        } else if (edited.length() == 13) {
            edited = edited.substring(0, 4) + "B";
        } else if (edited.length() == 14) {
            edited = edited.substring(0, 5) + "B";
        } else if (edited.length() == 15) {
            edited = edited.substring(0, 6) + "B";
        } else if (edited.length() == 17) {
            edited = edited.substring(0, 4) + "T";
        } else if (edited.length() == 18) {
            edited = edited.substring(0, 5) + "T";
        } else if (edited.length() == 19) {
            edited = edited.substring(0, 6) + "T";
        } else {
            edited = value + "";
        }

        return edited;
    }

    public double getDoubleFromStringwithSuffix(String text) {
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

    public String formatTimeString(long millis) {
        return formatTimeString(millis, true, true);
    }

    public String formatTimeString(long millis, final boolean showSeconds, final boolean showMilliseconds) {
        int days = 0, hours = 0, minutes = 0, seconds = 0;

        while (millis >= 1000) {
            seconds++;
            millis -= 1000;
        }

        while (seconds >= 60) {
            minutes++;
            seconds -= 60;
        }

        while (minutes >= 60) {
            hours++;
            minutes -= 60;
        }

        while (hours >= 24) {
            days++;
            hours -= 24;
        }

        return (days > 0 ? days + "d " : "") + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + (showSeconds ? (seconds > 0 ? seconds + "." + (millis / 100) + "s" : (showMilliseconds ? "0." + (millis / 100) + "s" : "")) : "");
    }

    public String formatNumber(final long value) {
        return decimalFormat.format(value);
    }

    public String formatNumber(final double value) {
        return decimalFormat.format(value);
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

    public net.kyori.adventure.text.TextComponent translateComponent(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        Matcher matcher = HEX_PATTERN.matcher(message);
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
                Matcher nextMatcher = HEX_PATTERN.matcher(message.substring(nextColorStart));
                if (nextMatcher.lookingAt()) {
                    break;
                }

                nextColorStart++;
            }

            String coloredText = message.substring(textStart, nextColorStart);

            if (!coloredText.isEmpty()) {
                net.kyori.adventure.text.TextComponent colorComponent = Component.text().content(coloredText).build();
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

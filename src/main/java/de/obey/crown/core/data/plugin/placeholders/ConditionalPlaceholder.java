package de.obey.crown.core.data.plugin.placeholders;


/*
    Author: Obey
    Date: 24.05.2026
    Time: 11:50
    Project: CrownCore
*/

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class ConditionalPlaceholder {

    public enum ConditionType {
        EQUALS("=="),
        NOT_EQUALS("!="),
        CONTAINS("contains"),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS(">="),
        LESS_THAN_OR_EQUALS("<=");

        private final String symbol;

        ConditionType(final String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static ConditionType fromSymbol(final String symbol) {
            for (final ConditionType type : values()) {
                if (type.symbol.equalsIgnoreCase(symbol)) return type;
            }
            throw new IllegalArgumentException("Unknown condition type: '" + symbol + "'");
        }
    }


    private final String id;
    private final ConditionType type;
    private final String input;
    private final String value;
    private final String output;
    private final String elseOutput;


    public ConditionalPlaceholder(final String id, final ConfigurationSection section) {
        this.id = id;
        this.type = ConditionType.fromSymbol(section.getString("type", "=="));
        this.input = section.getString("input", "");
        this.value = section.getString("value", "");
        this.output = section.getString("output", "");
        this.elseOutput = section.getString("else", "");
    }

    public String evaluate(final Player player) {
        final String resolvedInput = PlaceholderAPI.setPlaceholders(player, input);
        final String resolvedValue = PlaceholderAPI.setPlaceholders(player, value);

        final boolean conditionMet = switch (type) {
            case EQUALS                  -> resolvedInput.equals(resolvedValue);
            case NOT_EQUALS              -> !resolvedInput.equals(resolvedValue);
            case CONTAINS                -> resolvedInput.contains(resolvedValue);
            case GREATER_THAN            -> compareNumbers(resolvedInput, resolvedValue) > 0;
            case LESS_THAN               -> compareNumbers(resolvedInput, resolvedValue) < 0;
            case GREATER_THAN_OR_EQUALS  -> compareNumbers(resolvedInput, resolvedValue) >= 0;
            case LESS_THAN_OR_EQUALS     -> compareNumbers(resolvedInput, resolvedValue) <= 0;
        };

        final String result = conditionMet ? output : elseOutput;
        return PlaceholderAPI.setPlaceholders(player, result);
    }

    public String evaluate(final OfflinePlayer player) {
        final String resolvedInput = PlaceholderAPI.setPlaceholders(player, input);
        final String resolvedValue = PlaceholderAPI.setPlaceholders(player, value);

        final boolean conditionMet = switch (type) {
            case EQUALS                  -> resolvedInput.equals(resolvedValue);
            case NOT_EQUALS              -> !resolvedInput.equals(resolvedValue);
            case CONTAINS                -> resolvedInput.contains(resolvedValue);
            case GREATER_THAN            -> compareNumbers(resolvedInput, resolvedValue) > 0;
            case LESS_THAN               -> compareNumbers(resolvedInput, resolvedValue) < 0;
            case GREATER_THAN_OR_EQUALS  -> compareNumbers(resolvedInput, resolvedValue) >= 0;
            case LESS_THAN_OR_EQUALS     -> compareNumbers(resolvedInput, resolvedValue) <= 0;
        };

        final String result = conditionMet ? output : elseOutput;
        return PlaceholderAPI.setPlaceholders(player, result);
    }

    private double compareNumbers(final String a, final String b) {
        try {
            return Double.parseDouble(a) - Double.parseDouble(b);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

}

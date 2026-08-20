package de.obey.crown.core.gui.util;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:23
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.gui.model.GuiItemClickAction;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.ItemBuilder;
import de.obey.crown.core.util.PlaceholderUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiItemParser {

    public static GuiItem parse(final ConfigurationSection section, final String guiKey, final int guiSize, final List<String> defaultFlags) {
        final String itemKey = section.getName();

        final boolean add = section.getBoolean("add", false);

        if (!add && !section.contains("slot") && !section.contains("slots")) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Missing required field 'slot', 'slots' or 'add: true' for item '" + itemKey +
                            "' in GUI " + guiKey
            );
        }
        GuiValidation.require(section, "material", "item '" + itemKey + "' in GUI " + guiKey);

        final List<Integer> slots = new ArrayList<>();
        if (!add) {
            final List<Integer> rawSlots = new ArrayList<>();
            if (section.contains("slots")) {
                rawSlots.addAll(section.getIntegerList("slots"));
            } else {
                rawSlots.add(section.getInt("slot"));
            }

            for (final int slot : rawSlots) {
                if (GuiValidation.validateSlot(guiKey, itemKey, slot, guiSize)) {
                    slots.add(slot);
                }
            }
        }

        final String materialName = section.getString("material", "");
        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Invalid material '" + materialName + "' for item '" + itemKey +
                            "' in GUI " + guiKey
            );
        }

        final ItemBuilder builder = new ItemBuilder(material);

        CrownCore.log.debug("[CrownGUI] [ItemParser] Parsing name: " + section.getString("name"));

        builder.name(section.getString("name"))
                .lore(section.getStringList("lore"))
                .amount(section.getInt("amount", 1))
                .glow(section.getBoolean("glow", false))
                .customModelData(section.contains("custom-model-data")
                        ? section.getInt("custom-model-data")
                        : -231
                );

        if (material == Material.PLAYER_HEAD && section.contains("texture")) {
            builder.skullTexture(section.getString("texture"), UUID.randomUUID());
        }

        if (material == Material.PLAYER_HEAD && section.contains("owner")) {
            builder.skullOwner(section.getString("owner"));
        }

        parseEnchantments(section, builder, itemKey, guiKey);
        parseFlags(section, builder, itemKey, guiKey, defaultFlags);

        final ConfigurationSection leftClickSection = section.contains("left-click")
                ? section.getConfigurationSection("left-click")
                : section.getConfigurationSection("click");
        final GuiItemClickAction clickAction = parseClick(leftClickSection, itemKey, guiKey);
        final GuiItemClickAction rightClickAction = parseClick(section.getConfigurationSection("right-click"), itemKey, guiKey);

        final String action = section.getString("action");
        final String permission = section.getString("permission");

        return new GuiItem(
                slots,
                add,
                builder,
                clickAction,
                rightClickAction,
                action,
                permission
        );
    }

    private static void parseEnchantments(final ConfigurationSection section, final ItemBuilder builder, final String itemKey, final String guiKey) {
        if (!section.contains("enchantments")) return;

        final ConfigurationSection enchSection =
                section.getConfigurationSection("enchantments");

        if (enchSection == null) return;

        enchSection.getKeys(false).forEach(key -> {
            final Enchantment enchant = Enchantment.getByKey(
                    NamespacedKey.minecraft(key.toLowerCase())
            );

            if (enchant == null) {
                GuiValidation.warn(
                        "[CrownGUI] Unknown enchantment '" + key +
                                "' on item '" + itemKey + "' in GUI " + guiKey
                );
                return;
            }

            builder.enchant(enchant, enchSection.getInt(key));
        });
    }

    private static void parseFlags(final ConfigurationSection section, final ItemBuilder builder, final String itemKey, final String guiKey, final List<String> defaultFlags) {
        final List<String> flags = section.contains("flags") ? section.getStringList("flags") : defaultFlags;
        if (flags == null) return;

        for (final String flagName : flags) {
            try {
                final ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                builder.flag(flag);
            } catch (final IllegalArgumentException ex) {
                GuiValidation.warn(
                        "[CrownGUI] Invalid item flag '" + flagName +
                                "' on item '" + itemKey + "' in GUI " + guiKey
                );
            }
        }
    }

    private static GuiItemClickAction parseClick(final ConfigurationSection section, final String itemKey, final String guiKey) {
        if (section == null) {
            return null;
        }

        final String typeStr = section.getString("type", "NONE");
        GuiItemClickAction.Type type;
        try {
            type = GuiItemClickAction.Type.valueOf(typeStr.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            GuiValidation.warn("[CrownGUI] Invalid click type '" + typeStr + "' on item '" + itemKey + "' in GUI " + guiKey);
            type = GuiItemClickAction.Type.NONE;
        }

        final String value = section.getString("value");
        final boolean close = section.getBoolean("close", false);

        return new GuiItemClickAction(type, value, close);
    }


}

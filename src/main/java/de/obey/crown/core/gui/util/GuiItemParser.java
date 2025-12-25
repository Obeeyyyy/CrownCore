package de.obey.crown.core.gui.util;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:23
    Project: CrownCore
*/

import de.obey.crown.core.gui.model.GuiItem;
import de.obey.crown.core.gui.model.GuiItemClickAction;
import de.obey.crown.core.util.ItemBuilder;
import de.obey.crown.core.util.PlaceholderUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.UUID;

public class GuiItemParser {

    public static GuiItem parse(final ConfigurationSection section, final String guiKey, final int guiSize) {
        final String itemKey = section.getName();

        GuiValidation.require(section, "slot", itemKey);
        GuiValidation.require(section, "material", itemKey);

        final int slot = section.getInt("slot");
        GuiValidation.validateSlot(guiKey, itemKey, slot, guiSize);

        final Material material = Material.matchMaterial(
                section.getString("material", "")
        );

        if (material == null) {
            throw new IllegalArgumentException(
                    "[CrownGUI] Invalid material for item '" + itemKey +
                            "' in GUI " + guiKey
            );
        }

        final ItemBuilder builder = new ItemBuilder(material);

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

        parseEnchantments(section, builder, itemKey);
        parseFlags(section, builder, itemKey);

        final GuiItemClickAction clickAction = parseClick(section.getConfigurationSection("click"));
        final String permission = section.getString("permission");

        return new GuiItem(
                slot,
                builder,
                clickAction,
                permission
        );
    }

    private static void parseEnchantments(final ConfigurationSection section, final ItemBuilder builder, final String itemKey) {
        if (!section.contains("enchantments")) return;

        ConfigurationSection enchSection =
                section.getConfigurationSection("enchantments");

        if (enchSection == null) return;

        enchSection.getKeys(false).forEach(key -> {
            final Enchantment enchant = Enchantment.getByKey(
                    NamespacedKey.minecraft(key.toLowerCase())
            );

            if (enchant == null) {
                GuiValidation.warn(
                        "unknown enchantment '" + key +
                                "' on item '" + itemKey + "'"
                );
                return;
            }

            builder.enchant(enchant, enchSection.getInt(key));
        });
    }

    private static void parseFlags(final ConfigurationSection section, final ItemBuilder builder, final String itemKey) {
        if (!section.contains("flags")) return;

        for (final String flagName : section.getStringList("flags")) {
            try {
                final ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                builder.flag(flag);
            } catch (final IllegalArgumentException ex) {
                GuiValidation.warn(
                        "invalid item flag '" + flagName +
                                "' on item '" + itemKey + "'"
                );
            }
        }
    }

    private static GuiItemClickAction parseClick(final ConfigurationSection section) {
        if (section == null) {
            return new GuiItemClickAction(GuiItemClickAction.Type.NONE, null, false);
        }

        GuiItemClickAction.Type type;
        try {
            type = GuiItemClickAction.Type.valueOf(
                    section.getString("type", "NONE").toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            type = GuiItemClickAction.Type.NONE;
        }

        final String value = section.getString("value");
        final boolean close = section.getBoolean("close", false);

        return new GuiItemClickAction(type, value, close);
    }


}

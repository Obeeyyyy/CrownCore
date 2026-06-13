package de.obey.crown.core.util;


/*
    Author: Obey
    Date: 07.06.2026
    Time: 20:47
    Project: CrownCore
*/

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConfigItemStack {

    private Material material = Material.STONE;
    private int amount = 1;
    private String name = null;
    private List<String> lore = new ArrayList<>();
    private Integer customModelData = null;
    private boolean unbreakable = false;
    private boolean glow = false;
    private String skullOwner = null;
    private String skullTexture = null;
    private final Set<ItemFlag> flags = new LinkedHashSet<>();
    private final Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();

    public ConfigItemStack(final YamlConfiguration configuration, final String path) {
        this(configuration.getConfigurationSection(path));
    }

    public ConfigItemStack(final ConfigurationSection section) {
        if (section == null) return;

        material = FileUtil.getMaterial(section, "material", Material.STONE);

        if (section.contains("amount"))
            amount = section.getInt("amount");

        if (section.contains("name"))
            name = section.getString("name");

        if (section.contains("lore"))
            lore = section.getStringList("lore");

        if (section.contains("custom-model-data"))
            customModelData = section.getInt("custom-model-data");

        if (section.contains("unbreakable"))
            unbreakable = section.getBoolean("unbreakable");

        if (section.contains("glow"))
            glow = section.getBoolean("glow");

        if (section.contains("owner"))
            skullOwner = section.getString("owner");

        if (section.contains("texture"))
            skullTexture = section.getString("texture");

        if (section.contains("flags")) {
            for (String flagName : section.getStringList("flags")) {
                try {
                    flags.add(ItemFlag.valueOf(flagName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (section.contains("enchantments")) {
            ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
            if (enchantSection != null) {
                for (String key : enchantSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                    if (enchantment != null) {
                        enchantments.put(enchantment, enchantSection.getInt(key));
                    }
                }
            }
        }
    }

    public ItemBuilder toItemBuilder() {
        ItemBuilder builder = new ItemBuilder(material, amount);

        if (name != null) builder.name(name);
        if (!lore.isEmpty()) builder.lore(new ArrayList<>(lore));
        if (customModelData != null) builder.customModelData(customModelData);

        builder.unbreakable(unbreakable);
        builder.glow(glow);

        if (skullOwner != null) builder.skullOwner(skullOwner);
        if (skullTexture != null) builder.skullTexture(skullTexture, UUID.randomUUID());

        flags.forEach(builder::flag);
        enchantments.forEach(builder::enchant);

        return builder;
    }

    public ItemStack build() {
        return toItemBuilder().build();
    }

    public ItemStack build(final OfflinePlayer player) {
        return toItemBuilder().build(player);
    }

    public ItemStack build(final String[] placeholders, final String... replacements) {
        return build(null, placeholders, replacements);
    }

    public ItemStack build(final OfflinePlayer player, final String[] placeholders, final String... replacements) {
        final ItemBuilder builder = toItemBuilder();

        if (name != null) {
            String resolvedName = name;
            for (int i = 0; i < placeholders.length; i++) {
                if (replacements.length <= i) break;
                resolvedName = resolvedName.replace("%" + placeholders[i] + "%", String.valueOf(replacements[i]));
            }
            builder.name(resolvedName);
        }

        if (!lore.isEmpty()) {
            final List<String> resolvedLore = new ArrayList<>();
            for (String line : lore) {
                for (int i = 0; i < placeholders.length; i++) {
                    if (replacements.length <= i) break;
                    line = line.replace("%" + placeholders[i] + "%", String.valueOf(replacements[i]));
                }
                resolvedLore.add(line);
            }
            builder.lore(resolvedLore);
        }

        return builder.build(player);
    }

}

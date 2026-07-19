/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ItemBuilder {

    /* base */
    private final Material material;
    private int amount = 1;

    /* display */
    private String name;
    private List<String> lore;

    /* visuals */
    private boolean glow;
    private Boolean enchantmentGlintOverride;
    private Integer customModelData;
    private Color leatherColor;
    private Color fireworkColor;

    /* meta */
    private boolean unbreakable;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;

    /* skull */
    private String skullOwner;
    private String skullTexture;
    private UUID skullUUID;

    /* enchants & flags */
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private final Set<ItemFlag> flags = new HashSet<>();

    /* persistent data */
    private final Map<NamespacedKey, Object> persistentData = new LinkedHashMap<>();

    public ItemBuilder(final Material material) {
        this.material = material;
    }

    public ItemBuilder(final Material material, final int amount) {
        this.material = material;
        this.amount = Math.max(1, amount);
    }

    public ItemBuilder(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();

        this.material = item.getType();
        this.amount   = item.getAmount();

        if (meta == null) return;

        /* display */
        if (meta.hasDisplayName())
            this.name = MiniMessage.miniMessage().serialize(meta.displayName());

        /* lore */
        if (meta.hasLore())
            this.lore = meta.lore().stream()
                    .map(MiniMessage.miniMessage()::serialize)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        /* enchants */
        enchantments.putAll(meta.getEnchants());

        /* flags */
        flags.addAll(meta.getItemFlags());

        /* custom model data */
        if (meta.hasCustomModelData())
            this.customModelData = meta.getCustomModelData();

        /* unbreakable */
        this.unbreakable = meta.isUnbreakable();

        /* enchantment glint override */
        if (meta.hasEnchantmentGlintOverride())
            this.enchantmentGlintOverride = meta.getEnchantmentGlintOverride();

        /* glow */
        if (enchantmentGlintOverride == null
                && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
                && meta.hasEnchant(Enchantment.UNBREAKING)) {
            this.glow = true;
            enchantments.remove(Enchantment.UNBREAKING);
        }

        /* attribute modifiers */
        final Multimap<Attribute, AttributeModifier> attrs = meta.getAttributeModifiers();
        if (attrs != null && !attrs.isEmpty())
            this.attributeModifiers = LinkedHashMultimap.create(attrs);

        /* leather color */
        if (meta instanceof LeatherArmorMeta leather)
            this.leatherColor = leather.getColor();

        /* firework color */
        if (meta instanceof FireworkEffectMeta firework && firework.hasEffect())
            this.fireworkColor = firework.getEffect().getColors().isEmpty()
                    ? null
                    : firework.getEffect().getColors().get(0);

        /* skull */
        if (meta instanceof SkullMeta skull) {
            if (skull.getOwningPlayer() != null)
                this.skullOwner = skull.getOwningPlayer().getName();

            if (skull.getOwnerProfile() != null) {
                final PlayerTextures textures = skull.getOwnerProfile().getTextures();
                if (textures.getSkin() != null)
                    this.skullUUID = skull.getOwnerProfile().getUniqueId();
            }
        }

        /* persistent data */
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (final NamespacedKey key : pdc.getKeys())
            readPdcKey(pdc, key);
    }

    /* setters */

    public ItemBuilder amount(final int amount) {
        this.amount = Math.max(1, amount);
        return this;
    }

    @Deprecated
    public ItemBuilder setDisplayname(final String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(final String name) {
        this.name = name;
        return this;
    }

    @Deprecated
    public ItemBuilder setLore(final List<String> lore) {
        this.lore = lore;
        return this;
    }

    @Deprecated
    public ItemBuilder setLore(final String... lore) {
        this.lore = new ArrayList<>();
        Collections.addAll(this.lore, lore);
        return this;
    }

    public ItemBuilder lore(final String... lore) {
        this.lore = new ArrayList<>();
        Collections.addAll(this.lore, lore);
        return this;
    }

    public ItemBuilder lore(final List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder addLore(final List<String> extra) {
        if (this.lore == null) this.lore = new ArrayList<>();
        this.lore.addAll(extra);
        return this;
    }

    public ItemBuilder glow(final boolean glow) {
        this.glow = glow;
        return this;
    }

    public ItemBuilder glintOverride(final boolean glint) {
        this.enchantmentGlintOverride = glint;
        return this;
    }

    public ItemBuilder unbreakable(final boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder attribute(final Attribute attribute, final AttributeModifier modifier) {
        if (this.attributeModifiers == null)
            this.attributeModifiers = LinkedHashMultimap.create();
        this.attributeModifiers.put(attribute, modifier);
        return this;
    }

    public ItemBuilder customModelData(final Integer data) {
        this.customModelData = data;
        return this;
    }

    public ItemBuilder color(final DyeColor color) {
        this.leatherColor = color.getColor();
        return this;
    }

    public ItemBuilder color(final Color color) {
        this.leatherColor = color;
        return this;
    }

    public ItemBuilder fireworkColor(final Color color) {
        this.fireworkColor = color;
        return this;
    }

    public ItemBuilder skullOwner(final String name) {
        this.skullOwner = name;
        return this;
    }

    @Deprecated
    public ItemBuilder setSkullOwner(final String name) {
        this.skullOwner = name;
        return this;
    }

    public ItemBuilder skullTexture(final String texture, final UUID uuid) {
        this.skullTexture = texture;
        this.skullUUID    = uuid;
        return this;
    }

    @Deprecated
    public ItemBuilder setTextur(final String texture, final UUID uuid) {
        this.skullTexture = texture;
        this.skullUUID    = uuid;
        return this;
    }

    public ItemBuilder enchant(final Enchantment enchant, final int level) {
        enchantments.put(enchant, level);
        return this;
    }

    public ItemBuilder flag(final ItemFlag flag) {
        flags.add(flag);
        return this;
    }

    /* build */

    public ItemStack build() {
        return buildInternal(null);
    }

    public ItemStack build(final OfflinePlayer player) {
        return buildInternal(player);
    }

    private ItemStack buildInternal(final OfflinePlayer player) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta  meta = item.getItemMeta();
        if (meta == null) return item;

        /* display */
        if (name != null) {
            String resolved = player == null ? name : PlaceholderUtil.resolve(player, name);
            resolved = "<i:false>" + TextUtil.convertLegacyToMiniMessage(resolved);
            meta.displayName(MiniMessage.miniMessage().deserialize(resolved));
        }

        /* lore */
        if (lore != null) {
            final List<Component> resolvedLore = new ArrayList<>();
            for (final String line : lore) {
                String resolved = player == null ? line : PlaceholderUtil.resolve(player, line);
                resolved = "<i:false>" + TextUtil.convertLegacyToMiniMessage(resolved);
                resolvedLore.add(MiniMessage.miniMessage().deserialize(resolved));
            }
            meta.lore(resolvedLore);
        }

        /* enchants */
        enchantments.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));

        /* glow */
        if (enchantmentGlintOverride != null) {
            meta.setEnchantmentGlintOverride(enchantmentGlintOverride);
        } else if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        /* flags */
        flags.forEach(meta::addItemFlags);

        /* unbreakable */
        if (unbreakable)
            meta.setUnbreakable(true);

        /* attribute modifiers */
        if (attributeModifiers != null) {
            meta.setAttributeModifiers(attributeModifiers);
        } else if (meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
            for (final Attribute attribute : new Attribute[]{
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    Attribute.GENERIC_ATTACK_SPEED,
                    Attribute.GENERIC_ARMOR,
                    Attribute.GENERIC_ARMOR_TOUGHNESS,
                    Attribute.GENERIC_MAX_HEALTH,
                    Attribute.GENERIC_KNOCKBACK_RESISTANCE
            }) {
                meta.addAttributeModifier(
                        attribute,
                        new AttributeModifier(
                                new NamespacedKey("crownplugins", "zero_" + attribute.getKey().getKey()),
                                -1.0,
                                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                                EquipmentSlotGroup.ANY
                        )
                );
            }
        }

        /* custom model data */
        if (customModelData != null)
            meta.setCustomModelData(customModelData);

        /* leather armor */
        if (leatherColor != null && meta instanceof LeatherArmorMeta leather)
            leather.setColor(leatherColor);

        /* firework */
        if (fireworkColor != null && meta instanceof FireworkEffectMeta firework) {
            firework.setEffect(FireworkEffect.builder().withColor(fireworkColor).build());
            firework.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        /* skull owner */
        if (skullOwner != null && meta instanceof SkullMeta skull) {
            final String resolved = player == null ? skullOwner : PlaceholderUtil.resolve(player, skullOwner);
            skull.setOwner(resolved);
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(resolved));
        }

        /* skull texture */
        if (skullTexture != null && meta instanceof SkullMeta skull)
            applySkullTexture(skull);

        /* persistent data */
        applyPersistentData(meta);

        item.setItemMeta(meta);
        return item;
    }

    public ItemMeta buildMeta() {
        return buildMeta(null);
    }

    public ItemMeta buildMeta(final OfflinePlayer player) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta  meta = item.getItemMeta();
        if (meta == null) return null;

        /* display */
        if (name != null) {
            String resolved = player == null ? name : PlaceholderUtil.resolve(player, name);
            resolved = "<i:false>" + TextUtil.convertLegacyToMiniMessage(resolved);
            meta.displayName(MiniMessage.miniMessage().deserialize(resolved));
        }

        /* lore */
        if (lore != null) {
            final List<Component> resolvedLore = new ArrayList<>();
            for (final String line : lore) {
                String resolved = player == null ? line : PlaceholderUtil.resolve(player, line);
                resolved = "<i:false>" + TextUtil.convertLegacyToMiniMessage(resolved);
                resolvedLore.add(MiniMessage.miniMessage().deserialize(resolved));
            }
            meta.lore(resolvedLore);
        }

        /* enchants */
        enchantments.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));

        /* glow */
        if (enchantmentGlintOverride != null) {
            meta.setEnchantmentGlintOverride(enchantmentGlintOverride);
        } else if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        /* flags */
        flags.forEach(meta::addItemFlags);

        /* unbreakable */
        if (unbreakable)
            meta.setUnbreakable(true);

        /* attribute modifiers */
        if (attributeModifiers != null) {
            meta.setAttributeModifiers(attributeModifiers);
        } else if (meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
            for (final Attribute attribute : new Attribute[]{
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    Attribute.GENERIC_ATTACK_SPEED,
                    Attribute.GENERIC_ARMOR,
                    Attribute.GENERIC_ARMOR_TOUGHNESS,
                    Attribute.GENERIC_MAX_HEALTH,
                    Attribute.GENERIC_KNOCKBACK_RESISTANCE
            }) {
                meta.addAttributeModifier(
                        attribute,
                        new AttributeModifier(
                                new NamespacedKey("crownplugins", "zero_" + attribute.getKey().getKey()),
                                -1.0,
                                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                                EquipmentSlotGroup.ANY
                        )
                );
            }
        }

        /* custom model data */
        if (customModelData != null)
            meta.setCustomModelData(customModelData);

        /* leather armor */
        if (leatherColor != null && meta instanceof LeatherArmorMeta leather)
            leather.setColor(leatherColor);

        /* firework */
        if (fireworkColor != null && meta instanceof FireworkEffectMeta firework) {
            firework.setEffect(FireworkEffect.builder().withColor(fireworkColor).build());
            firework.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        /* skull owner */
        if (skullOwner != null && meta instanceof SkullMeta skull) {
            final String resolved = player == null ? skullOwner : PlaceholderUtil.resolve(player, skullOwner);
            skull.setOwner(resolved);
            //skull.setOwningPlayer(Bukkit.getOfflinePlayer(resolved));
        }

        /* skull texture */
        if (skullTexture != null && meta instanceof SkullMeta skull)
            applySkullTexture(skull);

        /* persistent data */
        applyPersistentData(meta);
        return meta;
    }

    /* skull util */

    private void applySkullTexture(final SkullMeta skull) {
        final PlayerProfile  profile  = Bukkit.createPlayerProfile(
                skullUUID != null ? skullUUID : UUID.randomUUID(), "crownplugins");
        final PlayerTextures textures = profile.getTextures();

        final String full = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + skullTexture;

        try {
            final byte[]     decoded = Base64.getDecoder().decode(full);
            final JsonObject json    = new Gson().fromJson(
                    new String(decoded, StandardCharsets.UTF_8), JsonObject.class);

            final String url = json
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url").getAsString();

            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            skull.setOwnerProfile(profile);

        } catch (final Exception ignored) {}
    }

    /* persistent data */

    public ItemBuilder pdc(final NamespacedKey key, final String value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final boolean value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final int value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final long value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final double value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final byte value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final int[] value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final long[] value) {
        persistentData.put(key, value);
        return this;
    }

    public ItemBuilder pdc(final NamespacedKey key, final byte[] value) {
        persistentData.put(key, value);
        return this;
    }

    private void applyPersistentData(final ItemMeta meta) {
        if (persistentData.isEmpty()) return;
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();

        for (final Map.Entry<NamespacedKey, Object> entry : persistentData.entrySet()) {
            final NamespacedKey key = entry.getKey();
            switch (entry.getValue()) {
                case String  v -> pdc.set(key, PersistentDataType.STRING,        v);
                case Boolean v -> pdc.set(key, PersistentDataType.BOOLEAN,       v);
                case Integer v -> pdc.set(key, PersistentDataType.INTEGER,       v);
                case Long    v -> pdc.set(key, PersistentDataType.LONG,          v);
                case Double  v -> pdc.set(key, PersistentDataType.DOUBLE,        v);
                case Byte    v -> pdc.set(key, PersistentDataType.BYTE,          v);
                case int[]   v -> pdc.set(key, PersistentDataType.INTEGER_ARRAY, v);
                case long[]  v -> pdc.set(key, PersistentDataType.LONG_ARRAY,    v);
                case byte[]  v -> pdc.set(key, PersistentDataType.BYTE_ARRAY,    v);
                default -> throw new IllegalArgumentException(
                        "Unsupported PDC type: " + entry.getValue().getClass().getName());
            }
        }
    }

    private void readPdcKey(final PersistentDataContainer pdc, final NamespacedKey key) {
        if      (pdc.has(key, PersistentDataType.STRING))        persistentData.put(key, pdc.get(key, PersistentDataType.STRING));
        else if (pdc.has(key, PersistentDataType.BOOLEAN))       persistentData.put(key, pdc.get(key, PersistentDataType.BOOLEAN));
        else if (pdc.has(key, PersistentDataType.INTEGER))       persistentData.put(key, pdc.get(key, PersistentDataType.INTEGER));
        else if (pdc.has(key, PersistentDataType.LONG))          persistentData.put(key, pdc.get(key, PersistentDataType.LONG));
        else if (pdc.has(key, PersistentDataType.DOUBLE))        persistentData.put(key, pdc.get(key, PersistentDataType.DOUBLE));
        else if (pdc.has(key, PersistentDataType.BYTE))          persistentData.put(key, pdc.get(key, PersistentDataType.BYTE));
        else if (pdc.has(key, PersistentDataType.INTEGER_ARRAY)) persistentData.put(key, pdc.get(key, PersistentDataType.INTEGER_ARRAY));
        else if (pdc.has(key, PersistentDataType.LONG_ARRAY))    persistentData.put(key, pdc.get(key, PersistentDataType.LONG_ARRAY));
        else if (pdc.has(key, PersistentDataType.BYTE_ARRAY))    persistentData.put(key, pdc.get(key, PersistentDataType.BYTE_ARRAY));
    }

    public ItemBuilder clone() {
        final ItemBuilder clone = new ItemBuilder(this.material, this.amount);
        clone.name = this.name;
        if (this.lore != null) {
            clone.lore = new ArrayList<>(this.lore);
        }
        clone.glow = this.glow;
        clone.enchantmentGlintOverride = this.enchantmentGlintOverride;
        clone.customModelData = this.customModelData;
        clone.leatherColor = this.leatherColor;
        clone.fireworkColor = this.fireworkColor;
        clone.skullOwner = this.skullOwner;
        clone.skullTexture = this.skullTexture;
        clone.skullUUID = this.skullUUID;
        clone.enchantments.putAll(this.enchantments);
        clone.flags.addAll(this.flags);
        clone.persistentData.putAll(this.persistentData);
        if (this.attributeModifiers != null) {
            clone.attributeModifiers = LinkedHashMultimap.create(this.attributeModifiers);
        }
        return clone;
    }

    public void resolvePlaceholders(final String[] placeholders, final String... replacements) {
        if (placeholders == null || replacements == null || placeholders.length == 0) {
            return;
        }
        if (name != null) {
            for (int i = 0; i < placeholders.length; i++) {
                name = name.replace("%" + placeholders[i] + "%", replacements[i]);
            }
        }
        if (lore != null) {
            final List<String> newLore = new ArrayList<>();
            for (String line : lore) {
                for (int i = 0; i < placeholders.length; i++) {
                    line = line.replace("%" + placeholders[i] + "%", replacements[i]);
                }
                newLore.add(line);
            }
            this.lore = newLore;
        }
        if (skullOwner != null) {
            for (int i = 0; i < placeholders.length; i++) {
                skullOwner = skullOwner.replace("%" + placeholders[i] + "%", replacements[i]);
            }
        }
    }
}
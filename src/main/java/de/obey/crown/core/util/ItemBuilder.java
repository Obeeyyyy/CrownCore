/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
    private Integer customModelData;
    private DyeColor leatherColor;
    private Color fireworkColor;

    /* skull */
    private String skullOwner;
    private String skullTexture;
    private UUID skullUUID;

    /* enchants & flags */
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private final Set<ItemFlag> flags = new HashSet<>();

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(Material material, int amount) {
        this.material = material;
        this.amount = Math.max(1, amount);
    }

    /* settters */

    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, amount);
        return this;
    }

    /* for backwards comp*/
    @Deprecated
    public ItemBuilder setDisplayname(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    /* for backwards comp*/
    @Deprecated
    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    /* for backwards comp*/
    @Deprecated
    public ItemBuilder setLore(String... lore) {
        this.lore.clear();
        Collections.addAll(this.lore, lore);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        this.lore.clear();
        Collections.addAll(this.lore, lore);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder addLore(List<String> extra) {
        if (this.lore == null)
            this.lore = new ArrayList<>();
        this.lore.addAll(extra);
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public ItemBuilder customModelData(Integer data) {
        this.customModelData = data;
        return this;
    }

    public ItemBuilder color(DyeColor color) {
        this.leatherColor = color;
        return this;
    }

    public ItemBuilder fireworkColor(Color color) {
        this.fireworkColor = color;
        return this;
    }

    public ItemBuilder skullOwner(String name) {
        this.skullOwner = name;
        return this;
    }

    /* for backwards comp*/
    @Deprecated
    public ItemBuilder setSkullOwner(String name) {
        this.skullOwner = name;
        return this;
    }

    public ItemBuilder skullTexture(String texture, UUID uuid) {
        this.skullTexture = texture;
        this.skullUUID = uuid;
        return this;
    }

    /* for backwards comp*/
    @Deprecated
    public ItemBuilder setTextur(String texture, UUID uuid) {
        this.skullTexture = texture;
        this.skullUUID = uuid;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchant, int level) {
        enchantments.put(enchant, level);
        return this;
    }

    public ItemBuilder flag(ItemFlag flag) {
        flags.add(flag);
        return this;
    }

    /* build functions */

    public ItemStack build() {
        return buildInternal(null);
    }

    public ItemStack build(final OfflinePlayer player) {
        return buildInternal(player);
    }

    private ItemStack buildInternal(final OfflinePlayer player) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        /* display */
        if (name != null) {
            final String resolved = player == null ? name : PlaceholderUtil.resolve(player, name);
            meta.displayName(TextUtil.translateComponent(resolved));
        }

        /* lore */
        if (lore != null) {
            final List<String> resolvedLore = new ArrayList<>();
            for (String line : lore) {
                resolvedLore.add(TextUtil.translateColors(player == null ? line : PlaceholderUtil.resolve(player, line)));
            }
            meta.setLore(resolvedLore);
        }

        /* enchants */
        enchantments.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));

        if (glow) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        /* flags */
        flags.forEach(meta::addItemFlags);

        /* custom model data */
        if (customModelData != null)
            meta.setCustomModelData(customModelData);

        /* leather armor */
        if (leatherColor != null && meta instanceof LeatherArmorMeta leather) {
            leather.setColor(leatherColor.getColor());
        }

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
        if (skullTexture != null && meta instanceof SkullMeta skull) {
            applySkullTexture(skull);
        }

        item.setItemMeta(meta);
        return item;
    }

    /* skull util*/

    private void applySkullTexture(SkullMeta skull) {
        final PlayerProfile profile = Bukkit.createPlayerProfile(
                skullUUID != null ? skullUUID : UUID.randomUUID(),
                "crownplugins"
        );

        final PlayerTextures textures = profile.getTextures();
        final String full = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + skullTexture;

        try {
            byte[] decoded = Base64.getDecoder().decode(full);
            JsonObject json = new Gson().fromJson(
                    new String(decoded, StandardCharsets.UTF_8),
                    JsonObject.class
            );

            String url = json
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString();

            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            skull.setOwnerProfile(profile);

        } catch (final Exception ignored) {}
    }
}

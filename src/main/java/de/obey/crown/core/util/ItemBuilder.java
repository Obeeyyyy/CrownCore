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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ItemBuilder {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    private final ItemStack itemStack;
    private final ItemMeta meta;

    private FireworkEffectMeta fireworkMeta;
    private SkullMeta skullMeta;
    private LeatherArmorMeta leatherMeta;

    public ItemBuilder(final ItemStack itemStack) {
        this.itemStack = itemStack;
        meta = itemStack.getItemMeta();

        setupCorrectItemMeta();
    }

    public ItemBuilder(final Material material) {
        itemStack = new ItemStack(material);
        meta = itemStack.getItemMeta();

        setupCorrectItemMeta();
    }

    public ItemBuilder(final Material material, final int amount) {
        itemStack = new ItemStack(material, amount);
        meta = itemStack.getItemMeta();

        setupCorrectItemMeta();
    }

    private void setupCorrectItemMeta() {
        if (itemStack.getType() == Material.FIRE_CHARGE)
            fireworkMeta = (FireworkEffectMeta) itemStack.getItemMeta();

        if (itemStack.getType() == Material.PLAYER_HEAD)
            skullMeta = (SkullMeta) itemStack.getItemMeta();

        if (itemStack.getType().name().contains("LEATHER_"))
            leatherMeta = (LeatherArmorMeta) itemStack.getItemMeta();
    }

    public ItemBuilder setDisplayname(String name) {
        name = TextUtil.translateColors(name);

        if(meta != null)
            meta.setDisplayName(name);

        if (fireworkMeta != null)
            fireworkMeta.setDisplayName(name);

        if (skullMeta != null)
            skullMeta.setDisplayName(name);

        if (leatherMeta != null)
            leatherMeta.setDisplayName(name);

        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(final String... lore) {
        final List<String> list = new ArrayList<>();

        for (String line : lore) {
            line = TextUtil.translateColors(line);
            list.add(line);
        }

        if(meta != null)
            meta.setLore(list);

        if (fireworkMeta != null)
            fireworkMeta.setLore(list);

        if (skullMeta != null)
            skullMeta.setLore(list);

        if (leatherMeta != null)
            leatherMeta.setLore(list);

        return this;
    }

    public ItemBuilder setLore(final List<String> list) {
        meta.setLore(list);

        if (fireworkMeta != null)
            fireworkMeta.setLore(list);

        if (skullMeta != null)
            skullMeta.setLore(list);

        if (leatherMeta != null)
            leatherMeta.setLore(list);

        return this;
    }

    public ItemBuilder addLore(final String... lore) {
        final List<String> list = meta.getLore() == null ? new ArrayList<>() : meta.getLore();

        Collections.addAll(list, lore);

        if(meta != null)
            meta.setLore(list);

        if (fireworkMeta != null)
            fireworkMeta.setLore(list);

        if (skullMeta != null)
            skullMeta.setLore(list);

        if (leatherMeta != null)
            leatherMeta.setLore(list);

        return this;
    }

    public ItemBuilder addLore(final List<String> lore) {
        final List<String> list = meta.getLore() == null ? new ArrayList<>() : meta.getLore();

        list.addAll(lore);

        if(meta != null)
            meta.setLore(list);

        if (fireworkMeta != null)
            fireworkMeta.setLore(list);

        if (skullMeta != null)
            skullMeta.setLore(list);

        if (leatherMeta != null)
            leatherMeta.setLore(list);

        return this;
    }

    public ItemBuilder setFireWorkColor(final Color color) {
        final FireworkEffect effect = FireworkEffect.builder().withColor(color).build();

        fireworkMeta = (FireworkEffectMeta) meta;
        fireworkMeta.setEffect(effect);
        fireworkMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        return this;
    }

    public ItemBuilder setSkullOwner(final String name) {

        if (skullMeta != null) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            skullMeta.setOwningPlayer(offlinePlayer);
        }

        return this;
    }

    public ItemBuilder setTexturURL(final String url, final UUID uuid) {
        if (skullMeta == null)
            return this;

        final PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid, "crownplugins");
        final PlayerTextures playerTextures = playerProfile.getTextures();

        try {
            playerTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + url));

            playerProfile.setTextures(playerTextures);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public ItemBuilder setTextur(final String texture, final UUID uuid) {

        if (skullMeta == null)
            return this;

        final PlayerProfile profile = Bukkit.createPlayerProfile(uuid, "crownplugins");
        final PlayerTextures textures = profile.getTextures();
        final String fullTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + texture;

        // Decode the Base64 string
        byte[] decodedBytes = Base64.getDecoder().decode(fullTexture);
        // Parse the JSON object

        final JsonObject json = new Gson().fromJson(new String(decodedBytes, StandardCharsets.UTF_8), JsonObject.class);
        final JsonObject texturesJson = json.getAsJsonObject("textures");
        final JsonObject skinJson = texturesJson.getAsJsonObject("SKIN");

        String textureUrl = skinJson.get("url").getAsString();

        try {
            textures.setSkin(new URL(textureUrl));
        } catch (MalformedURLException ignored) {
        }

        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);

        return this;
    }

    public ItemBuilder setColor(final DyeColor color) {
        if (leatherMeta == null)
            return this;

        leatherMeta.setColor(color.getColor());

        return this;
    }

    public ItemBuilder addEnchantment(final Enchantment enchantment, final int level) {
        if(meta != null)
            meta.addEnchant(enchantment, level, true);

        if (fireworkMeta != null)
            fireworkMeta.addEnchant(enchantment, level, true);

        if (skullMeta != null)
            skullMeta.addEnchant(enchantment, level, true);

        if (leatherMeta != null)
            leatherMeta.addEnchant(enchantment, level, true);

        return this;
    }

    public ItemBuilder addEnchantment(final Enchantment enchantment) {
        if(meta != null)
            meta.addEnchant(enchantment, 1, true);

        if (fireworkMeta != null)
            fireworkMeta.addEnchant(enchantment, 1, true);

        if (skullMeta != null)
            skullMeta.addEnchant(enchantment, 1, true);

        if (leatherMeta != null)
            leatherMeta.addEnchant(enchantment, 1, true);

        return this;
    }

    public ItemBuilder setEnchantments(final Map<Enchantment, Integer> enchantments) {
        enchantments.keySet().forEach(enchantment -> {
            meta.addEnchant(enchantment, enchantments.get(enchantment), true);

            if (leatherMeta != null)
                leatherMeta.addEnchant(enchantment, enchantments.get(enchantment), true);
        });

        return this;
    }

    public ItemBuilder addItemFlags(final ItemFlag... flags) {
        if(meta != null)
            meta.addItemFlags(flags);

        if (fireworkMeta != null)
            fireworkMeta.addItemFlags(flags);

        if (skullMeta != null)
            skullMeta.addItemFlags(flags);

        if (leatherMeta != null)
            leatherMeta.addItemFlags(flags);

        return this;
    }

    public ItemBuilder setCustomModelData(final int data) {
        meta.setCustomModelData(data);

        return this;
    }
    
    public ItemStack build() {
        itemStack.setItemMeta(meta);

        if (fireworkMeta != null)
            itemStack.setItemMeta(fireworkMeta);

        if (skullMeta != null)
            itemStack.setItemMeta(skullMeta);

        if (leatherMeta != null)
            itemStack.setItemMeta(leatherMeta);

        return itemStack;
    }

}

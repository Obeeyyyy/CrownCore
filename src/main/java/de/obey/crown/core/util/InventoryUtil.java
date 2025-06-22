/* CrownPlugins - CrownCore */
/* 17.08.2024 - 01:29 */

package de.obey.crown.core.util;

import de.obey.crown.core.nobf.CrownCore;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

@UtilityClass
public final class InventoryUtil {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    public void fillFromTo(final Inventory inventory, final ItemStack itemStack, int from, int to) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= from && i <= to)
                inventory.setItem(i, itemStack);
        }
    }

    public void fill(final Inventory inventory, final ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, itemStack);
    }

    public void fillSideRows(final Inventory inventory, final ItemStack itemStack) {

        if (inventory.getSize() >= 9) {
            inventory.setItem(0, itemStack);
            inventory.setItem(8, itemStack);
        }

        if (inventory.getSize() >= 18) {
            inventory.setItem(9, itemStack);
            inventory.setItem(17, itemStack);
        }

        if (inventory.getSize() >= 27) {
            inventory.setItem(18, itemStack);
            inventory.setItem(26, itemStack);
        }

        if (inventory.getSize() >= 36) {
            inventory.setItem(27, itemStack);
            inventory.setItem(35, itemStack);
        }

        if (inventory.getSize() >= 45) {
            inventory.setItem(36, itemStack);
            inventory.setItem(44, itemStack);
        }

        if (inventory.getSize() >= 54) {
            inventory.setItem(45, itemStack);
            inventory.setItem(53, itemStack);
        }

        if (inventory.getSize() >= 63) {
            inventory.setItem(54, itemStack);
            inventory.setItem(62, itemStack);
        }
    }

    public void addItemToPlayer(final Player player, final ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                player.getInventory().setItemInOffHand(item);
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getWorld().dropItem(player.getLocation(), item.clone());
                }
            }.runTask(CrownCore.getInstance());
            return;
        }

        player.getInventory().addItem(item);
    }

    public void addItemToPlayer(final Player player, final ItemStack item, final int amount) {

        item.setAmount(amount);

        if (player.getInventory().firstEmpty() == -1) {
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                player.getInventory().setItemInOffHand(item);
                return;
            }

            player.getWorld().dropItem(player.getLocation(), item.clone());
            return;
        }

        player.getInventory().addItem(item.clone());
    }

    public void removeItemInMainHand(final Player player, final int amount) {
        if (player.getInventory().getItemInMainHand().getAmount() <= amount) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            final ItemStack item = player.getInventory().getItemInMainHand().clone();

            item.setAmount(item.getAmount() - amount);
            player.getInventory().setItemInMainHand(item);
        }
    }

    public boolean hasItemInHandWithName(final Player player, final String name) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
            return false;

        if (!item.hasItemMeta())
            return false;

        final ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return false;

        if (!meta.hasDisplayName())
            return false;

        return meta.getDisplayName().equalsIgnoreCase(name);
    }

    public boolean hasItemInHand(final Player player) {
        return player.getInventory().getItemInMainHand().getType() != Material.AIR;
    }

}

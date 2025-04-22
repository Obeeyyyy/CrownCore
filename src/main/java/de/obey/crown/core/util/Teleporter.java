package de.obey.crown.core.util;
/*

    Author - Obey -> TraxFight
       16.07.2021 / 18:12

*/

import com.google.common.collect.Maps;
import de.obey.crown.core.CrownCore;
import de.obey.crown.core.PluginConfig;
import de.obey.crown.core.data.plugin.Messanger;
import de.obey.crown.core.data.plugin.TeleportMessageType;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.util.effects.TeleportEffect;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class Teleporter {

    private final ArrayList<UUID> isTeleporting = new ArrayList<UUID>();
    private final Map<Player, BossBar> bossaBars = Maps.newConcurrentMap();

    private PluginConfig crownPluginConfig;
    private Messanger messanger;

    public void initialize() {
        crownPluginConfig = CrownCore.getInstance().getPluginConfig();
        messanger = crownPluginConfig.getMessanger();
    }

    public void teleportInstant(final Player player, final String locationName) {
        final Location location = LocationHandler.getLocation(locationName);
        if (location == null) {
            messanger.sendMessage(player, "location-invalid", new String[]{"name"}, locationName);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 3f);
            return;
        }

        player.teleport(location);
        player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 0.5f, 3f);
        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 0.5f, 3f);
    }

    public void teleportInstant(final Player player, final Location location) {
        if (location == null) {
            return;
        }

        player.teleport(location);
        player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 0.5f, 3f);
        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 0.5f, 3f);
    }

    public void teleportWithAnimation(final Player player, final String locationName) {
        final Location location = LocationHandler.getLocation(locationName);
        if (location == null) {
            messanger.sendMessage(player, "location-invalid", new String[]{"name"}, locationName);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 3f);
            return;
        }

        teleportWithAnimation(player, location);
    }

    public void teleportWithAnimation(final Player player, final Location location) {

        if (crownPluginConfig.isInstantTeleport()) {
            teleportInstant(player, location);
            return;
        }

        if (isTeleporting.contains(player.getUniqueId()))
            return;

        final long cooldown = crownPluginConfig.getTeleportDelay() * 1000L;

        if (location == null)
            return;


        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            teleportInstant(player, location);
            return;
        }

        if (crownPluginConfig.getInstantTeleportWorlds().contains(player.getWorld().getName())) {
            teleportInstant(player, location);
            return;
        }

        final TeleportEffect effect = new TeleportEffect(Particle.CHERRY_LEAVES, 5);

        sendTeleportMessage(player, 0, cooldown, cooldown);

        isTeleporting.add(player.getUniqueId());
        effect.run(CrownCore.getInstance(), player, 1, null);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 0.5f, 0.6f);

        new BukkitRunnable() {

            final Location saved = player.getLocation();
            long remain = cooldown;
            int ticks = 0;
            int microticks = 0;
            float pitch = 0.6f;

            @Override
            public void run() {

                if (player.getLocation().getX() != saved.getX() || player.getLocation().getZ() != saved.getZ()) {
                    removeBossbar(player);
                    effect.stop();
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.2f, 3f);
                    messanger.sendMessage(player, "teleportation-cancelled");
                    isTeleporting.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (microticks < 20) {
                    microticks++;
                    remain -= 50;

                    sendTeleportMessage(player, ticks, cooldown, remain);
                    return;
                }

                microticks = 0;

                if ((ticks + 1) >= crownPluginConfig.getTeleportDelay()) {
                    teleportInstant(player, location);
                    sendTeleportCompletedMessage(player);
                    removeBossbar(player);
                    isTeleporting.remove(player.getUniqueId());
                    effect.stop();
                    cancel();
                    return;
                }

                pitch += 0.1f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 0.5f, pitch);

                ticks++;
            }
        }.runTaskTimer(CrownCore.getInstance(), 1, 1);
    }

    private void removeBossbar(final Player player) {
        if (bossaBars.containsKey(player)) {
            bossaBars.get(player).removeAll();
            bossaBars.remove(player);
        }
    }

    private void sendTeleportCompletedMessage(final Player player) {
        if (crownPluginConfig.getTeleportMessageType() == TeleportMessageType.BOSSBAR) {

            if (bossaBars.containsKey(player)) {
                final BossBar bossBar = bossaBars.get(player);
                bossBar.setTitle(messanger.getMessage("teleported-message"));
                return;
            }

            final BossBar bossBar = Bukkit.createBossBar(messanger.getMessage("teleported-message"),
                    BarColor.BLUE, BarStyle.SEGMENTED_10);

            bossBar.setProgress(0);
            bossBar.addPlayer(player);

            bossaBars.put(player, bossBar);
        } else {
            messanger.sendActionbar(player, messanger.getMessage("teleported-message"));
        }
    }

    private void sendTeleportMessage(final Player player, final int ticks, final long cooldown, final long remaining) {
        if (crownPluginConfig.getTeleportMessageType() == TeleportMessageType.BOSSBAR) {

            if (bossaBars.containsKey(player)) {
                final BossBar bossBar = bossaBars.get(player);

                bossBar.setProgress((double) ticks / (cooldown / 1000d));
                bossBar.setTitle(messanger.getMessage("telportation-message", new String[]{"remaining"}, TextUtil.formatTimeString(remaining)));

                return;
            }

            final BossBar bossBar = Bukkit.createBossBar(messanger.getMessage("telportation-message", new String[]{"remaining"}, TextUtil.formatTimeString(cooldown)),
                    BarColor.BLUE, BarStyle.SEGMENTED_10);

            bossBar.setProgress(0);
            bossBar.addPlayer(player);

            bossaBars.put(player, bossBar);
        } else {
            messanger.sendActionbar(player, messanger.getMessage("telportation-message", new String[]{"remaining"}, TextUtil.formatTimeString(remaining)));
        }
    }

}

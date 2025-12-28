package de.obey.crown.core.util;
/*

    Author - Obey -> TraxFight
       16.07.2021 / 18:12

*/

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.noobf.PluginConfig;
import de.obey.crown.core.data.plugin.Messanger;
import de.obey.crown.core.data.plugin.TeleportMessageType;
import de.obey.crown.core.data.plugin.sound.SoundData;
import de.obey.crown.core.data.plugin.sound.Sounds;
import de.obey.crown.core.handler.LocationHandler;
import de.obey.crown.core.util.effects.TeleportEffect;
import de.obey.crown.core.util.task.CrownTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class Teleporter {

    private final List<UUID> isTeleporting = new ArrayList<UUID>();
    private final Map<Player, BossBar> bossBars = Maps.newConcurrentMap();
    @Getter
    private final Map<UUID, CrownTask> tasks = Maps.newConcurrentMap();

    private PluginConfig crownPluginConfig;
    private Messanger messanger;
    private Sounds sounds;

    public void initialize() {
        crownPluginConfig = CrownCore.getInstance().getPluginConfig();
        messanger = crownPluginConfig.getMessanger();
        sounds = crownPluginConfig.getSounds();
    }

    public void quit(final Player player) {
        if(tasks.containsKey(player.getUniqueId()))
            tasks.get(player.getUniqueId()).cancel();

        isTeleporting.remove(player.getUniqueId());
    }

    public void teleportInstant(final Player player, final String locationName) {
        final Location location = LocationHandler.getLocation(locationName);
        if (location == null) {
            messanger.sendMessage(player, "location-invalid", new String[]{"name"}, locationName);
            sounds.playSoundToPlayer(player, "location-invalid");
            return;
        }

        teleportInstant(player, location);
    }

    public void teleportInstant(final Player player, final Location location) {
        if (location == null)
            return;

        player.teleportAsync(location);

        sounds.playSoundToPlayer(player, "teleport-instant-1");
        sounds.playSoundToPlayer(player, "teleport-instant-2");
    }

    public void teleportWithAnimation(final Player player, final String locationName) {
        final Location location = LocationHandler.getLocation(locationName);
        if (location == null) {
            messanger.sendMessage(player, "location-invalid", new String[]{"name"}, locationName);
            sounds.playSoundToPlayer(player, "location-invalid");
            return;
        }

        teleportWithAnimation(player, location);
    }

    public void teleportWithAnimation(final Player player, final Location location) {
        if (location == null)
            return;

        if (crownPluginConfig.isInstantTeleport()) {
            teleportInstant(player, location);
            return;
        }

        if (player.hasPermission("core.instant.teleport")) {
            teleportInstant(player, location);
            return;
        }

        if (isTeleporting.contains(player.getUniqueId()))
            return;

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            teleportInstant(player, location);
            return;
        }

        if (crownPluginConfig.getInstantTeleportWorlds().contains(player.getWorld().getName())) {
            teleportInstant(player, location);
            return;
        }

        if (!crownPluginConfig.getInstantTeleportRegions().isEmpty()) {
            for (final String region : crownPluginConfig.getInstantTeleportRegions()) {
                if (WorldGuardUtil.isPlayerInRegion(player, region)) {
                    teleportInstant(player, location);
                    return;
                }
            }
        }

        final long cooldown = crownPluginConfig.getTeleportDelay() * 1000L;
        final TeleportEffect effect = new TeleportEffect(Particle.CHERRY_LEAVES, 5);

        sendTeleportMessage(player, 0, cooldown, cooldown);

        isTeleporting.add(player.getUniqueId());
        effect.run(CrownCore.getInstance(), player, 1, null);

        sounds.playSoundToPlayer(player, "teleport-tick");

        final TeleportState teleportState = new TeleportState(player.getLocation());
        teleportState.setRemain(cooldown);

        tasks.put(player.getUniqueId(), Scheduler.runEntityTaskTimer(CrownCore.getInstance(), player, () -> {
            if (player.getLocation().getX() != teleportState.getSaved().getX() || player.getLocation().getZ() != teleportState.getSaved().getZ()) {
                removeBossbar(player);
                effect.stop();
                sounds.playSoundToPlayer(player, "teleport-cancelled");
                messanger.sendMessage(player, "teleportation-cancelled");
                isTeleporting.remove(player.getUniqueId());
                tasks.get(player.getUniqueId()).cancel();
                return;
            }

            if (teleportState.getMicroTicks() < 20) {
                teleportState.setMicroTicks(teleportState.getMicroTicks() + 1);
                teleportState.setRemain(teleportState.getRemain() - 50);

                sendTeleportMessage(player, teleportState.getTicks(), cooldown, teleportState.getRemain());
                return;
            }

            teleportState.setMicroTicks(0);

            if ((teleportState.getTicks() + 1) >= crownPluginConfig.getTeleportDelay()) {
                teleportInstant(player, location);
                sendTeleportCompletedMessage(player);
                removeBossbar(player);
                isTeleporting.remove(player.getUniqueId());
                effect.stop();
                tasks.get(player.getUniqueId()).cancel();
                return;
            }

            teleportState.setPitch(teleportState.getPitch() + 0.1f);
            final SoundData soundData = sounds.getSoundData("teleport-tick");
            player.playSound(player.getLocation(), soundData.getSound(), soundData.getVolume(), teleportState.getPitch());

            teleportState.setTicks(teleportState.getTicks() + 1);
        }, 1, 1));
    }

    private void removeBossbar(final Player player) {
        if (bossBars.containsKey(player)) {
            bossBars.get(player).removeAll();
            bossBars.remove(player);
        }
    }

    private void sendTeleportCompletedMessage(final Player player) {
        if (crownPluginConfig.getTeleportMessageType() == TeleportMessageType.BOSSBAR) {

            if (bossBars.containsKey(player)) {
                final BossBar bossBar = bossBars.get(player);
                bossBar.setTitle(messanger.getMessage("teleported-message"));
                return;
            }

            final BossBar bossBar = Bukkit.createBossBar(messanger.getMessage("teleported-message"),
                    BarColor.BLUE, BarStyle.SEGMENTED_10);

            bossBar.setProgress(0);
            bossBar.addPlayer(player);

            bossBars.put(player, bossBar);
        } else {
            messanger.sendActionbar(player, "teleported-message");
        }
    }

    private void sendTeleportMessage(final Player player, final int ticks, final long cooldown, final long remaining) {
        if (crownPluginConfig.getTeleportMessageType() == TeleportMessageType.BOSSBAR) {

            if (bossBars.containsKey(player)) {
                final BossBar bossBar = bossBars.get(player);

                bossBar.setProgress((double) ticks / (cooldown / 1000d));
                bossBar.setTitle(messanger.getMessage("telportation-message", new String[]{"remaining"}, TextUtil.formatTimeStringWithFormat(remaining, crownPluginConfig.getTeleportationTimeFormat())));

                return;
            }

            final BossBar bossBar = Bukkit.createBossBar(messanger.getMessage("teleportation-message", new String[]{"remaining"}, TextUtil.formatTimeStringWithFormat(cooldown, crownPluginConfig.getTeleportationTimeFormat())),
                    BarColor.BLUE, BarStyle.SEGMENTED_10);

            bossBar.setProgress(0);
            bossBar.addPlayer(player);

            bossBars.put(player, bossBar);
        } else {
            messanger.sendActionbar(player, "teleportation-message", new String[]{"remaining"}, TextUtil.formatTimeStringWithFormat(remaining, crownPluginConfig.getTeleportationTimeFormat()));
        }
    }
}

@RequiredArgsConstructor
@Getter
@Setter
class TeleportState {
    private final Location saved;
    private long remain;
    private int ticks = 0;
    private int microTicks = 0;
    private float pitch = 0.6f;
}

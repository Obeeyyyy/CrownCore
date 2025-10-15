package de.obey.crown.core.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@UtilityClass
public class WorldGuardUtil {

    public boolean isPlayerInRegion(final Player player, final String regionName) {

        if(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return false;
        }

        final Location loc = player.getLocation();
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(loc));

        for (final ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }

        return false;
    }

}

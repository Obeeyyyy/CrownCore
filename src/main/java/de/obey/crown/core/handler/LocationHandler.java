/* CrownPlugins - CrownCore */
/* 21.08.2024 - 01:33 */

package de.obey.crown.core.handler;

import com.google.common.collect.Maps;
import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.TextUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public final class LocationHandler {

    @Getter
    private Map<String, Location> locations;

    public Location getLocation(final String name) {
        return locations.get(name);
    }

    public void setLocation(final String name, final Location location) {
        locations.put(name, location);
    }

    public void deleteLocation(final String name) {
        locations.remove(name);
        saveLocations();
    }

    /***
     * Converts location data from config.yml into locations.yml
     */
    public YamlConfiguration convertLocations() {
        locations = Maps.newConcurrentMap();

        final File file = FileUtil.getFile(CrownCore.getInstance().getDataFolder().getPath() + "/", "config.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        final File locationsFile = FileUtil.getCreatedFile(CrownCore.getInstance(), "locations.yml", true);
        final YamlConfiguration locationsConfiguration = YamlConfiguration.loadConfiguration(locationsFile);

        if(!configuration.contains("locations")) {
            return locationsConfiguration;
        }


        for (final String key : configuration.getConfigurationSection("locations").getKeys(false)) {
            locationsConfiguration.set("locations." + key, configuration.getString("locations." + key));
        }

        configuration.set("locations", null);

        FileUtil.saveConfigurationIntoFile(configuration, file);
        FileUtil.saveConfigurationIntoFile(locationsConfiguration, locationsFile);

        return locationsConfiguration;
    }

    /***
     * Loads location data from locations.yml file
     */
    public void loadLocations() {
        final YamlConfiguration configuration = convertLocations();

        if (!configuration.contains("locations")) {
            return;
        }

        for (final String name : configuration.getConfigurationSection("locations").getKeys(false)) {
            locations.put(name, TextUtil.parseStringToLocation(configuration.getString("locations." + name)));
        }
    }

    /***
     * Saves location data into locations.yml
     */
    public void saveLocations() {
        final File file = FileUtil.getCreatedFile(CrownCore.getInstance(), "locations.yml", true);
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if (locations.isEmpty()) {
            return;
        }

        configuration.set("locations", null);

        for (final String name : locations.keySet()) {
            configuration.set("locations." + name, TextUtil.parseLocationToString(locations.get(name)));
        }

        FileUtil.saveConfigurationIntoFile(configuration, file);
    }
}

/* CrownPlugins - CrownCore */
/* 18.08.2024 - 01:22 */

package de.obey.crown.core.nobf;

import de.obey.crown.core.data.plugin.CrownConfig;
import de.obey.crown.core.data.plugin.TeleportMessageType;
import de.obey.crown.core.util.FileUtil;
import de.obey.crown.core.util.Log;
import de.obey.crown.core.util.TextUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.LocaleUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

@Getter
@Setter
public final class PluginConfig extends CrownConfig {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    private TeleportMessageType teleportMessageType;

    private int teleportDelay, messageDelay, commandDelay;
    private boolean instantTeleport = false, instantRespawn = true, teleportOnJoin, updateReminder = true;
    private ArrayList<String> instantTeleportWorlds;

    public PluginConfig(@NonNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void loadConfig() {
        final File file = FileUtil.getFile(this.getPlugin().getDataFolder().getPath(), "config.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        setTeleportMessageType(FileUtil.getString(configuration, "teleport-message-type", "actionbar").equalsIgnoreCase("actionbar") ? TeleportMessageType.ACTIONBAR : TeleportMessageType.BOSSBAR);
        setInstantTeleport(FileUtil.getBoolean(configuration, "instant-teleport", false));
        setTeleportDelay(FileUtil.getInt(configuration, "teleport-delay", 10));
        setInstantTeleportWorlds(FileUtil.getStringArrayList(configuration, "instant-teleport-worlds", new ArrayList<>()));
        setMessageDelay(FileUtil.getInt(configuration, "message-cooldown", 0));
        setCommandDelay(FileUtil.getInt(configuration, "command-cooldown", 0));
        setUpdateReminder(FileUtil.getBoolean(configuration, "update-reminder", true));

        Log.setDebug(FileUtil.getBoolean(configuration, "debug-mode", false));

        Locale locale = LocaleUtils.toLocale(FileUtil.getString(configuration, "number-formatting", "en_US"));
        if(locale == null)
            locale = Locale.ENGLISH;

        TextUtil.setDecimalFormat(new DecimalFormat("#,###.##", new DecimalFormatSymbols(locale)));

        FileUtil.saveConfigurationIntoFile(configuration, file);
    }

    @Override
    public void saveConfig() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getConfigFile());

        configuration.set("debug-mode", Log.isDebug());

        FileUtil.saveConfigurationIntoFile(configuration, getConfigFile());
    }
}

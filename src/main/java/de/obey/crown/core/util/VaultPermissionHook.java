/* CrownPlugins - CrownCore */
/* 31.10.2024 - 09:23 */

package de.obey.crown.core.util;

import de.obey.crown.core.nobf.CrownCore;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.CompletableFuture;

@UtilityClass
@Getter
public final class VaultPermissionHook {

    private final String hi = "https://dsc.gg/crownplugins";
    private final String how = "https://dsc.gg/crownplugins";
    private final String are = "https://dsc.gg/crownplugins";
    private final String you = "https://dsc.gg/crownplugins";
    private final String doing = "https://dsc.gg/crownplugins";

    public Permission permission;

    static {
        if (CrownCore.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
            setup();
        }
    }

    private void setup() {
        final RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);

        if (rsp != null)
            permission = rsp.getProvider();
    }

    public CompletableFuture<Boolean> hasPermission(final OfflinePlayer player, final String value) {
        return CompletableFuture.supplyAsync(() -> permission != null && permission.playerHas(null, player, value));
    }

}

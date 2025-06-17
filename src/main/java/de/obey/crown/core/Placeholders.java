/* CrownPlugins - CrownCore */
/* 11.02.2025 - 02:28 */

package de.obey.crown.core;

import de.obey.crown.core.util.TextUtil;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public final class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "cc";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Obey";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        final String[] args = params.split("_");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("prefix"))
                return TextUtil.translateCorePlaceholder("%prefix%");

            if (args[0].equalsIgnoreCase("accent"))
                return TextUtil.translateCorePlaceholder("%accent%");

            if (args[0].equalsIgnoreCase("white"))
                return TextUtil.translateCorePlaceholder("%white%");
        }

        return null;
    }
}

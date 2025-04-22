/* CrownPlugins - CrownCore */
/* 22.04.2025 - 00:29 */

package de.obey.crown.core.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
public final class VersionChecker {

    private final String url = "https://versions.obeeyyyy.de/version/%plugin%/%version%";

    private final Map<String, String> newestVersions = Maps.newConcurrentMap();
    private final ArrayList<Plugin> outdatedPlugins = new ArrayList<>();

    public CompletableFuture<String> retrieveNewestVersion(final String pluginName, final String pluginVersion) {
        return CompletableFuture.supplyAsync(() -> {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.replace("%plugin%", pluginName).replace("%version%", pluginVersion)))
                    .GET()
                    .build();

            try {
                final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                final JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);

                if (!jsonResponse.has("newest"))
                    return pluginVersion;

                final String newestVersion = jsonResponse.get("newest").getAsString();
                newestVersions.put(pluginName, newestVersion);

                return newestVersion;

            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                return pluginVersion;
            }
        });
    }

    public CompletableFuture<Boolean> isNewestVersion(final Plugin plugin) {
        final String pluginName = plugin.getName();
        final String pluginVersion = plugin.getDescription().getVersion();

        if (newestVersions.containsKey(pluginName))
            return CompletableFuture.supplyAsync(() -> newestVersions.get(pluginName).equalsIgnoreCase(pluginVersion));

        return retrieveNewestVersion(pluginName, pluginVersion).thenApply((newestVersion) -> {
            final boolean isNewest = newestVersion.equalsIgnoreCase(pluginVersion);

            if (!isNewest)
                outdatedPlugins.add(plugin);

            return isNewest;
        });
    }

    public String getNewestVersion(final Plugin plugin) {
        return newestVersions.get(plugin.getName());
    }

}

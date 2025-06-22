/* CrownPlugins - CrownCore */
/* 22.04.2025 - 00:29 */

package de.obey.crown.core.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.obey.crown.core.nobf.CrownCore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Getter
public final class VersionChecker {

    private final ExecutorService executor;

    private final String singleUrl = "https://versions.obeeyyyy.de/version/%plugin%/%version%";
    private final String url = "https://versions.obeeyyyy.de/versions";

    private final Map<String, String> newestVersions = Maps.newConcurrentMap();
    private final ArrayList<Plugin> outdatedPlugins = new ArrayList<>();

    public void retrieveNewestPluginVersions() {
        CrownCore.getInstance().getExecutor().execute(() -> {
            final HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .executor(executor)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            try {
                final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                final JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);

                final Map<String, JsonElement> map = jsonResponse.asMap();

                for (final String pluginName : map.keySet()) {
                    newestVersions.put(pluginName, map.get(pluginName).getAsString());
                }
            } catch (JsonSyntaxException | IOException | InterruptedException ignored) {}
        });
    }

    public boolean isNewestVersion(final Plugin plugin) {
        final String pluginName = plugin.getName();
        final String pluginVersion = plugin.getDescription().getVersion();

        if (newestVersions.containsKey(pluginName))
            return newestVersions.get(pluginName).equalsIgnoreCase(pluginVersion);

        return true;
    }

    public String getNewestVersion(final Plugin plugin) {
        return newestVersions.get(plugin.getName());
    }

}

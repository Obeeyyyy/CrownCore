/* CrownPlugins - CrownCore */
/* 22.04.2025 - 00:29 */

package de.obey.crown.core.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.obey.crown.core.noobf.CrownCore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Getter
public final class VersionChecker {

    private final ExecutorService executor;
    private final OkHttpClient okHttpClient;

    private final String singleUrl = "https://versions.obeeyyyy.de/version/%plugin%/%version%";
    private final String url = "https://versions.obeeyyyy.de/versions";

    private final Map<String, String> newestVersions = Maps.newConcurrentMap();
    private final ArrayList<Plugin> outdatedPlugins = new ArrayList<>();

    public void retrieveNewestPluginVersions() {
        CrownCore.getInstance().getExecutor().execute(() -> {
            final Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (final Response response = okHttpClient.newCall(request).execute()) {
                final ResponseBody responseBody = response.body();
                if(responseBody == null)
                    return;

                final String bodyString = responseBody.string();

                CrownCore.log.debug(" versionchecker response: " + bodyString);

                final JsonObject jsonResponse = new Gson().fromJson(bodyString, JsonObject.class);
                final Map<String, JsonElement> map = jsonResponse.asMap();

                for (final String pluginName : map.keySet()) {
                    newestVersions.put(pluginName, map.get(pluginName).getAsString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean isNewestVersion(final Plugin plugin) {
        final String pluginName = plugin.getName();
        final String pluginVersion = plugin.getDescription().getVersion();

        if (newestVersions.containsKey(pluginName)) {

            try {
                final int currentValue = Integer.parseInt(pluginVersion.replace(".", ""));
                final int checkingAgainstValue = Integer.parseInt(newestVersions.get(pluginName).replace(".", ""));

                if (currentValue > checkingAgainstValue)
                    return true;
            } catch (final NumberFormatException ignored) {}

            return newestVersions.get(pluginName).equalsIgnoreCase(pluginVersion);
        }

        return true;
    }

    public String getNewestVersion(final Plugin plugin) {
        return newestVersions.get(plugin.getName());
    }

}

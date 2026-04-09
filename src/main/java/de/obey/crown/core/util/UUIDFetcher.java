package de.obey.crown.core.util;

/*

    Credits: Class provided by Max :)
      Modified by Obey.

 */

import de.obey.crown.core.noobf.CrownCore;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Pattern;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UUIDFetcher {

    private final Cache<String, UUID> NAME_TO_UNIQUE_ID_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();
    private final Cache<UUID, String> UNIQUE_ID_TO_NAME_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();

    private final List<Function<String, UUID>> NAME_TO_UNIQUE_ID_CONVERTER_LIST = new LinkedList<>();
    private final List<Function<UUID, String>> UNIQUE_ID_TO_NAME_CONVERTER_LIST = new LinkedList<>();

    @Setter
    private OkHttpClient okHttpClient;

    public void addNameToUniqueIdConverter(Function<String, UUID> function) {
        NAME_TO_UNIQUE_ID_CONVERTER_LIST.add(function);
    }

    public void addUniqueIdToNameConverter(Function<UUID, String> function) {
        UNIQUE_ID_TO_NAME_CONVERTER_LIST.add(function);
    }

    public void addToCache(final String username, final UUID uuid) {
        NAME_TO_UNIQUE_ID_CACHE.put(username, uuid);
        UNIQUE_ID_TO_NAME_CACHE.put(uuid, username);
    }

    public CompletableFuture<String> getUserNameAsync(@NonNull UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> getUserName(uniqueId), CrownCore.getInstance().getExecutor());
    }

    public CompletableFuture<UUID> resolveUUID(final String input) {
        CrownCore.log.debug("resolving uuid for '" + input + "'");

        try {
            final UUID uuid = UUID.fromString(input);
            CrownCore.log.debug(" - input already is uuid");
            return CompletableFuture.completedFuture(uuid);
        } catch (final IllegalArgumentException exception) {
            return getUniqueId(input);
        }
    }

    public @Nullable CompletableFuture<UUID> getUniqueId(@NotNull String username) {
        final boolean offlineMode = CrownCore.getInstance().getPluginConfig().isOfflineMode();

        if(!offlineMode)
            username = username.toLowerCase(Locale.ROOT);

        UUID uniqueId = NAME_TO_UNIQUE_ID_CACHE.getIfPresent(username);

        if (uniqueId != null)
            return CompletableFuture.completedFuture(uniqueId);

        CrownCore.log.debug("(!) fetching uuid for name " + username);
        CrownCore.log.debug(" -> offline-mode: " + offlineMode );

        if (!isValidMinecraftUserName(username)) {

            if(username.startsWith(FloodgateUtil.getBedrockPrefix())) {
                if(FloodgateUtil.isFloodgateEnabled()) {
                    CrownCore.log.debug(" -> name starts with bedrock prefix");

                    @NotNull final String finalUsername = username;
                    return FloodgateUtil.getUuidByName(username).thenApply((uuid) -> {
                        CrownCore.log.debug("   -> floodgate uuid: " + uuid);
                        NAME_TO_UNIQUE_ID_CACHE.put(finalUsername, uuid);
                        return uuid;
                    });
                } else {
                    CrownCore.log.warn(" You are trying to use bedrock features but the floodgate api is not present. Please install floodgate.");
                    return null;
                }
            }

            CrownCore.log.debug(" - name is invalid (not a minecraft or bedrock username)");
            return null;
        }

        if(offlineMode)
            return CompletableFuture.completedFuture(getOfflineUUID(username));

        String[] data = getRemoteUserData(username);

        if (data != null && data.length == 2) {
            uniqueId = UUID.fromString(data[1]);
            NAME_TO_UNIQUE_ID_CACHE.put(username, uniqueId);
            return CompletableFuture.completedFuture(uniqueId);
        }

        return null;
    }

    public @Nullable String getUserName(@NonNull UUID uniqueId) {
        String name = UNIQUE_ID_TO_NAME_CACHE.getIfPresent(uniqueId);
        if (name != null) {
            return name;
        }
        for (Function<UUID, String> function : UNIQUE_ID_TO_NAME_CONVERTER_LIST) {
            name = function.apply(uniqueId);
            if (name == null) {
                continue;
            }
            UNIQUE_ID_TO_NAME_CACHE.put(uniqueId, name);
            return name;
        }
        String[] data = getRemoteUserData(uniqueId.toString());
        if (data != null && data.length == 2) {
            name = data[0];
            UNIQUE_ID_TO_NAME_CACHE.put(uniqueId, name);
            return name;
        }
        return null;
    }

    private UUID getOfflineUUID(final String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private String[] getRemoteUserData(@NotNull String nameOrUuid) {
        try {

            final String url = "https://api.mojang.com/users/profiles/minecraft/" + nameOrUuid;

            CrownCore.log.debug(" - fetching from remote " + url);

            final Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (final Response response = okHttpClient.newCall(request).execute()) {
                final ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    CrownCore.log.debug(" - response body is null");
                    return null;
                }

                final String responseString = responseBody.string();

                CrownCore.log.debug(" - response: " + responseString);

                final JSONObject jsonObject = new JSONObject(responseString);

                if (!jsonObject.has("name") || !jsonObject.has("id")) {
                    CrownCore.log.debug(" - response is missing 'name' and 'id' objects");
                    return null;
                }

                if(!jsonObject.has("id")) {
                    CrownCore.log.debug(" - response is missing 'id' element");
                    return null;
                }

                final String uuidString = jsonObject.getString("id");
                if (uuidString == null || uuidString == JSONObject.NULL) {
                    CrownCore.log.debug(" - response is missing 'id' element");
                    return null;
                }

                final UUID uniqueId = validateUniqueId(uuidString);
                if (uniqueId == null) {
                    CrownCore.log.debug(" - response is missing 'id' element");
                    return null;
                }

                final String name = jsonObject.getString("name");

                CrownCore.log.debug(" - fetched");
                CrownCore.log.debug("   - name:" + name);
                CrownCore.log.debug("   - uuid:" + uuidString);

                return new String[]{name, uniqueId.toString()};
            }
        } catch (final Exception e) {
            CrownCore.log.debug(" - exception while fetching uuid " + e.getMessage());
        }
        return null;
    }

    private @Nullable UUID validateUniqueId(@NotNull String string) {
        CrownCore.log.debug(" - validating uuid: " + string);

        if (string.length() != 32) {
            CrownCore.log.debug(" - uuid is not 32 letters long.");
            return null;
        }
        if (string.contains("-")) {
            return UUID.fromString(string);
        }

        final String firstSeg = string.substring(0, 8); // 8
        final String secondSeg = string.substring(8, 12); // 4
        final String thirdSeg = string.substring(12, 16); // 4
        final String fourthSeg = string.substring(16, 20); // 4
        final String fifthSeg = string.substring(20, 32); // 12

        final UUID uuid = UUID.fromString(
                firstSeg + "-" + secondSeg + "-" + thirdSeg + "-" + fourthSeg + "-" + fifthSeg);

        CrownCore.log.debug(" - validated uuid " + uuid);

        return uuid;
    }

    private boolean isValidMinecraftUserName(@NotNull String userName) {
        return Pattern.matches("^[a-zA-Z0-9_][a-zA-Z0-9_\\\\-]*$", userName);
    }
}


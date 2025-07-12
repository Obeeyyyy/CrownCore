package de.obey.crown.core.util;

/*

    Credits: Class provided by Max :)
      Modified by Obey.

 */

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UUIDFetcher {

    private final Cache<String, UUID> NAME_TO_UNIQUE_ID_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();
    private final Cache<UUID, String> UNIQUE_ID_TO_NAME_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();

    private final List<Function<String, UUID>> NAME_TO_UNIQUE_ID_CONVERTER_LIST = new LinkedList<>();
    private final List<Function<UUID, String>> UNIQUE_ID_TO_NAME_CONVERTER_LIST = new LinkedList<>();

    private OkHttpClient okHttpClient;

    public void initHTTPClient(final OkHttpClient param) {
        okHttpClient = param;
    }

    public void addNameToUniqueIdConverter(Function<String, UUID> function) {
        NAME_TO_UNIQUE_ID_CONVERTER_LIST.add(function);
    }

    public void addUniqueIdToNameConverter(Function<UUID, String> function) {
        UNIQUE_ID_TO_NAME_CONVERTER_LIST.add(function);
    }

    public CompletableFuture<UUID> getUniqueIdAsync(@NonNull String userName) {
        return CompletableFuture.supplyAsync(() -> getUniqueId(userName), CrownCore.getInstance().getExecutor());
    }

    public CompletableFuture<String> getUserNameAsync(@NonNull UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> getUserName(uniqueId), CrownCore.getInstance().getExecutor());
    }


    public @Nullable UUID getUniqueId(@NotNull String userName) {
        CrownCore.log.debug("(!) fetching uuid for name " + userName);
        if (!isValidMinecraftUserName(userName)) {
            CrownCore.log.debug(" - name is invalid (not a minecraft username)");
            return null;
        }

        userName = userName.toLowerCase(Locale.ROOT);
        UUID uniqueId = NAME_TO_UNIQUE_ID_CACHE.getIfPresent(userName);
        if (uniqueId != null) {
            return uniqueId;
        }
        for (Function<String, UUID> function : NAME_TO_UNIQUE_ID_CONVERTER_LIST) {
            uniqueId = function.apply(userName);
            if (uniqueId == null) {
                continue;
            }
            NAME_TO_UNIQUE_ID_CACHE.put(userName, uniqueId);
            return uniqueId;
        }
        String[] data = getRemoteUserData(userName);
        if (data != null && data.length == 2) {
            uniqueId = UUID.fromString(data[1]);
            NAME_TO_UNIQUE_ID_CACHE.put(userName, uniqueId);
            return uniqueId;
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

                final JsonObject object = JsonParser.parseString(responseString).getAsJsonObject();


                if (object == null || !object.has("name") || !object.has("id")) {
                    CrownCore.log.debug(" - response is missing 'name' and 'id' objects");
                    return null;
                }

                final JsonElement uuidElement = object.get("id");
                if (uuidElement == null || uuidElement instanceof JsonNull) {
                    CrownCore.log.debug(" - response is missing 'id' element");
                    return null;
                }

                final String uniqueIdString = uuidElement.getAsString();
                final UUID uniqueId = validateUniqueId(uniqueIdString);
                if (uniqueId == null) {
                    CrownCore.log.debug(" - response is missing 'id' element");
                    return null;
                }

                final String name = object.get("name").getAsString();

                CrownCore.log.debug(" - fetched");
                CrownCore.log.debug("   - name:" + name);
                CrownCore.log.debug("   - uuid:" + uniqueIdString);

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


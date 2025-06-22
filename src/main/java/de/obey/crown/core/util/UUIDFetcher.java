package de.obey.crown.core.util;

/*

    Credits: Class provided by Max :)
      Modified by Obey.

 */

import de.obey.crown.core.nobf.CrownCore;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private HttpClient httpClient;

    public void initHTTPClient() {
        httpClient = HttpClient.newBuilder().executor(CrownCore.getInstance().getExecutor()).build();
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
        if (!isValidMinecraftUserName(userName)) {
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
            String url = "https://api.minetools.eu/uuid/" + nameOrUuid;
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "UUIDUtils-1.1")
                    .build();
            String result = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            if (result == null || result.isEmpty() || result.trim().isEmpty()) {
                return null;
            }
            JsonObject object = JsonParser.parseString(result).getAsJsonObject();
            if (object == null || !object.has("name") || !object.has("id")) {
                return null;
            }

            JsonElement uuidElement = object.get("id");
            if (uuidElement == null || uuidElement instanceof JsonNull) {
                return null;
            }

            String uniqueIdString = object.get("id").getAsString();
            UUID uniqueId = validateUniqueId(uniqueIdString);
            if (uniqueId == null) {
                return null;
            }
            String name = object.get("name").getAsString();
            return new String[]{name, uniqueId.toString()};
        } catch (Exception e) {
            if (!(e instanceof FileNotFoundException) && !(e instanceof SocketException)) {
                throw new RuntimeException("Couldn't find uuid of \"" + nameOrUuid + "\" at api.minetools.eu:", e);
            }
        }
        return null;
    }

    private @Nullable UUID validateUniqueId(@NotNull String string) {
        if (string.length() != 32) {
            return null;
        }
        if (string.contains("-")) {
            return UUID.fromString(string);
        }
        String firstSeg = string.substring(0, 8); // 8
        String secondSeg = string.substring(8, 12); // 4
        String thirdSeg = string.substring(12, 16); // 4
        String fourthSeg = string.substring(16, 20); // 4
        String fifthSeg = string.substring(20, 32); // 12
        return UUID.fromString(
                firstSeg + "-" + secondSeg + "-" + thirdSeg + "-" + fourthSeg + "-" + fifthSeg);
    }

    private boolean isValidMinecraftUserName(@NotNull String userName) {
        return Pattern.matches("^[a-zA-Z0-9_][a-zA-Z0-9_\\\\-]*$", userName);
    }
}


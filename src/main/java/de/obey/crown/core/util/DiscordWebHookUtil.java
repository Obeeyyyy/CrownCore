package de.obey.crown.core.util;


/*
    Author: Obey
    Date: 24.05.2026
    Time: 13:08
    Project: CrownCore
*/

import de.obey.crown.core.noobf.CrownCore;
import lombok.experimental.UtilityClass;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

@UtilityClass
public class DiscordWebHookUtil {

    public void post(final String webhookURL, final String content) {
        CrownCore.log.debug("posting webhook");
        CrownCore.log.debug(" - content: " + content);

        final JSONObject jsonObject = new JSONObject(content);

        if (jsonObject.has("embeds")) {
            final JSONArray embeds = jsonObject.getJSONArray("embeds");
            for (final Object embedObject : embeds) {
                final JSONObject embed = (JSONObject) embedObject;

                replaceNewlines(embed);

                if (embed.has("color")) {
                    embed.put("color", colorFrom(embed.getString("color")));
                }
            }
        }

        final RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
        final Request request = new Request.Builder()
                .url(webhookURL)
                .post(body)
                .build();

        try (final Response response = CrownCore.getInstance().getOkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                CrownCore.log.warn("error while sending embed");
                CrownCore.log.warn(" - code: " + response.code());
                CrownCore.log.warn(" - message: " + response.message());
                CrownCore.log.warn(" - sent body: " + jsonObject);
            }
        } catch (final IOException exception) {
            CrownCore.log.warn("exception while sending embed");
            CrownCore.log.warn(" - message: " + exception.getMessage());
            CrownCore.log.warn(" - sent body: " + jsonObject);
        }
    }

    private int colorFrom(String code) {
        if (code == null || code.isEmpty())
            return 0x000000;

        if (code.startsWith("#"))
            code = code.substring(1);

        try {
            if (code.length() == 6) {
                return Integer.parseInt(code, 16);
            } else if (code.length() == 8) {
                return Integer.parseInt(code.substring(2), 16);
            }
        } catch (final NumberFormatException ignored) {}

        return 0x000000;
    }

    private void replaceNewlines(final JSONObject object) {
        for (final String key : object.keySet()) {
            final Object value = object.get(key);

            if (value instanceof String str) {
                object.put(key, str.replace("%nl%", "\n"));
            } else if (value instanceof JSONObject nested) {
                replaceNewlines(nested);
            } else if (value instanceof JSONArray array) {
                replaceNewlines(array);
            }
        }
    }

    private void replaceNewlines(final JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            final Object value = array.get(i);

            if (value instanceof String str) {
                array.put(i, str.replace("%nl%", "\n"));
            } else if (value instanceof JSONObject nested) {
                replaceNewlines(nested);
            } else if (value instanceof JSONArray nestedArray) {
                replaceNewlines(nestedArray);
            }
        }
    }

}

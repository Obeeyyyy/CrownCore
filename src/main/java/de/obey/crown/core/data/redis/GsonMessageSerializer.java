package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 17:12
    Project: CrownCore
*/

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;

public class GsonMessageSerializer implements IMessageSerializer {

    private final Gson gson = new Gson();

    @Override
    public <T> String serialize(final T object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T deserialize(final String json, final Class<T> type) {
        return gson.fromJson(json, type);
    }
}

package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 16:59
    Project: CrownCore
*/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class RedisJson {

    public final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();
}

package de.obey.crown.core.data.redis;

/*
    Author: Obey
    Date: 02.01.2026
    Time: 17:12
    Project: CrownCore
*/

import lombok.AccessLevel;
import lombok.Getter;

public interface IMessageSerializer {

    <T> String serialize(final T object);
    <T> T deserialize(final String json, final Class<T> type);
}

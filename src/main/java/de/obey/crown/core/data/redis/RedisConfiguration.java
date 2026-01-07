package de.obey.crown.core.data.redis;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RedisConfiguration {

    private String host, username, password;
    private int port, database, timeout;
    private boolean ssl, enabled;

}

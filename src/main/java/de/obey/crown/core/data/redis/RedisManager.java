package de.obey.crown.core.data.redis;

import de.obey.crown.core.noobf.CrownCore;
import de.obey.crown.core.util.Scheduler;
import de.obey.crown.core.util.task.CrownTask;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionStateAdapter;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Getter
public class RedisManager {

    private final ExecutorService executor;

    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubConnection;
    private StatefulRedisPubSubConnection<String, String> subConnection;

    private RedisClient redisClient;

    private CrownTask heartbeatTask;

    private final java.util.List<Runnable> connectionHooks = new java.util.concurrent.CopyOnWriteArrayList<>();

    public void addConnectionHook(Runnable hook) {
        this.connectionHooks.add(hook);
    }

    public java.util.concurrent.CompletableFuture<Boolean> initialize() {
        return createConnection().thenApply(enable -> {
            if(enable) {
                if(heartbeatTask != null)
                    heartbeatTask.cancel();

                heartbeatTask = Scheduler.runAsyncTaskTimer(CrownCore.getInstance(), () -> {
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            connection.sync().ping();
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }, executor).thenAccept(alive -> {
                        if (!alive) {
                            CrownCore.log.warn("redis heartbeat failed — attempting reconnect");
                            createConnection();
                        }
                    });
                }, 20 * 10, 20 * 10);
            }
            return enable;
        });
    }

    /***
     * Initiates the redis connection with the settings read out of the config.yml
     */
    public CompletableFuture<Boolean> createConnection() {
        final RedisConfiguration redisConfiguration = CrownCore.getInstance().getPluginConfig().getRedisConfiguration();

        if(redisConfiguration == null || !redisConfiguration.isEnabled())
            return CompletableFuture.completedFuture(false);

        return CompletableFuture.supplyAsync(() -> {

            if (connection != null && connection.isOpen()) {
                // If connection exists, close it first to prevent leaks
                shutdown();
            }

            CrownCore.log.debug(" - creating new redis connections");

            final RedisURI redisURI = buildRedisUri(redisConfiguration);

            redisClient = RedisClient.create(redisURI);
            connection = redisClient.connect();
            pubConnection = redisClient.connectPubSub();
            subConnection = redisClient.connectPubSub();

            createListener();
            
            // execute hooks
            connectionHooks.forEach(Runnable::run);

            return true;
        }, executor);
    }

    public void shutdown() {
        try {
            if (connection != null && connection.isOpen()) connection.close();
            if (pubConnection != null && pubConnection.isOpen()) pubConnection.close();
            if (subConnection != null && subConnection.isOpen()) subConnection.close();
        } catch (final Exception ignored) { }

        if (redisClient != null) redisClient.shutdown();

        if(heartbeatTask != null)
            heartbeatTask.cancel();
    }

    private void createListener() {
        subConnection.addListener(new RedisConnectionStateAdapter() {

            @Override
            public void onRedisConnected(final RedisChannelHandler<?, ?> connection) {
                CrownCore.log.info("redis sub connected");
            }

            @Override
            public void onRedisDisconnected(final RedisChannelHandler<?, ?> connection) {
                CrownCore.log.warn("redis sub disconnected");
            }

            @Override
            public void onRedisExceptionCaught(final RedisChannelHandler<?, ?> connection, final Throwable cause) {
                CrownCore.log.warn("redis sub exception: " + cause.getMessage());
            }
        });
    }

    private RedisURI buildRedisUri(final RedisConfiguration configuration) {
        final RedisURI.Builder builder = RedisURI.builder()
                .withHost(configuration.getHost())
                .withPort(configuration.getPort())
                .withDatabase(configuration.getDatabase())
                .withTimeout(Duration.ofMillis(configuration.getTimeout()));

        if (configuration.isSsl())
            builder.withSsl(true);

        final String username = configuration.getUsername();
        final String password = configuration.getPassword();

        if (username != null && !username.isEmpty() &&
                password != null && !password.isEmpty()) {
            builder.withAuthentication(username, password);
        } else if (password != null && !password.isEmpty()) {
            builder.withPassword(password.toCharArray());
        }

        return builder.build();
    }
}

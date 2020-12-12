package jepsen.redis.internal;


import redis.clients.jedis.Jedis;

import java.util.Optional;

public class JepsenRedisRawClient implements JepsenRedisClient {

    private final Jedis redisConnection;

    public JepsenRedisRawClient(String hostname, int port) {
        redisConnection = new Jedis(hostname, port);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redisConnection.get(key));
    }

    @Override
    public void set(String key, String value) {
        redisConnection.set(key, value);
    }

    @Override
    public void del(String key) {
        redisConnection.del(key);
    }

    @Override
    public void close() {
        redisConnection.close();
    }
}

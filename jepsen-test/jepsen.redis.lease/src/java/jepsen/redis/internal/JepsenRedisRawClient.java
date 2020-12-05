package jepsen.redis.internal;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;

import java.util.Optional;

public class JepsenRedisRawClient implements JepsenRedisClient {

    private final RedisConnection<String, String> redisConnection;

    public JepsenRedisRawClient(String hostname, String port) {
        RedisURI redisURI = RedisURI.create("redis://" + hostname + ":" + port);
        RedisClient redisClient = new RedisClient(redisURI);
        redisConnection = redisClient.connect();
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

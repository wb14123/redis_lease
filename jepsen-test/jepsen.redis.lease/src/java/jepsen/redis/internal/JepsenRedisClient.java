package jepsen.redis.internal;

import java.util.Optional;

public interface JepsenRedisClient {
    Optional<String> get(String key);
    void set(String key, String value);
    void del(String key);
    void close();
}

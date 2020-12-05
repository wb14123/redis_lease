package jepsen.redis.internal;

import java.util.Optional;

public class JepsenClient {

    private final JepsenRedisClient cache;
    private final JepsenRedisClient db;

    public static JepsenClient singleRawCacheClient(
            String cacheHostName, String cachePortName, String dbHostName, String dbPortName) {
        JepsenRedisClient cache = new JepsenRedisRawClient(cacheHostName, cachePortName);
        JepsenRedisClient db = new JepsenRedisRawClient(dbHostName, dbPortName);
        return new JepsenClient(cache, db);
    }

    public JepsenClient(JepsenRedisClient cache, JepsenRedisClient db) {
        this.cache = cache;
        this.db = db;
    }

    public String get(String key) {
        return getOptional(key).orElse(null);
    }

    public Optional<String> getOptional(String key) {
        Optional<String> maybeValue = cache.get(key);
        if (maybeValue.isPresent()) {
            return maybeValue;
        }
        Optional<String> maybeDBValue = db.get(key);
        if (maybeDBValue.isEmpty()) {
            return maybeDBValue;
        }
        cache.set(key, maybeDBValue.get());
        return maybeDBValue;
    }

    public void set(String key, String value) {
        db.set(key, value);
        cache.del(key);
    }

    public void close() {
        cache.close();
        db.close();
    }

}

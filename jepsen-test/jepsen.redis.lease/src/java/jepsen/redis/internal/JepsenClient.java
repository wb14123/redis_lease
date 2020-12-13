package jepsen.redis.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JepsenClient {

    private static final Logger logger = LoggerFactory.getLogger(JepsenClient.class);
    private final JepsenRedisClient cache;
    private final JepsenRedisClient db;

    public static JepsenClient singleRawCacheClient(
            String cacheHostName, int cachePort, String dbHostName, int dbPort) {
        logger.info("Start raw client");
        JepsenRedisClient cache = new JepsenRedisRawClient(cacheHostName, cachePort);
        JepsenRedisClient db = new JepsenRedisRawClient(dbHostName, dbPort);
        return new JepsenClient(cache, db);
    }

    public static JepsenClient singleLeaseCacheClient(String cacheHostName, int cachePort, String dbHostName,
            int dbPort, String scriptDirectory) {
        logger.info("Start lease client");
        JepsenRedisClient cache = new JepsenRedisLeaseClient(cacheHostName, cachePort, scriptDirectory);
        JepsenRedisClient db = new JepsenRedisRawClient(dbHostName, dbPort);
        return new JepsenClient(cache, db);
    }

    public JepsenClient(JepsenRedisClient cache, JepsenRedisClient db) {
        this.cache = cache;
        this.db = db;
    }

    public String get(String key) {
        return getOptional(key).orElse(null);
    }

    public String getDB(String key) {
        return db.get(key).orElse(null);
    }

    public String getCache(String key) {
        return cache.get(key).orElse(null);
    }

    public void check(String key) {
        String dbKey = getDB(key);
        String cacheKey = getCache(key);
        if (cacheKey != null && !dbKey.equals(cacheKey)) {
            throw new RuntimeException("Check failed: key not match: " + cacheKey + "," + dbKey);
        }
        System.out.println("Check succeed");
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

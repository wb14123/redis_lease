package jepsen.redis.internal;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JepsenRedisLeaseClient implements JepsenRedisClient {

    private final Jedis redisConnection;
    private final String getFileSha1;
    private final String setFileSha1;
    private final String delFileSha1;

    private String lease;

    public JepsenRedisLeaseClient(String hostname, int port, String scriptDirectory) {
        redisConnection = new Jedis(hostname, port);
        this.getFileSha1 = loadScript(Path.of(scriptDirectory, "get.lua"));
        this.setFileSha1 = loadScript(Path.of(scriptDirectory, "set.lua"));
        this.delFileSha1 = loadScript(Path.of(scriptDirectory, "del.lua"));
    }

    private String getUUID() {
        return UUID.randomUUID().toString();
    }

    private String loadScript(Path file) {
        try {
            String script = Files.readString(file);
            return redisConnection.scriptLoad(script);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> get(String key) {
        List<String> result = (List<String>) redisConnection.evalsha(getFileSha1, 1, key, getUUID());
        lease = result.get(1);
        return Optional.ofNullable(result.get(0));
    }

    @Override
    public void set(String key, String value) {
        redisConnection.evalsha(setFileSha1, 1, key, lease, value);
    }

    @Override
    public void del(String key) {
        redisConnection.evalsha(delFileSha1, 1, key);
    }

    @Override
    public void close() {
        redisConnection.close();
    }
}

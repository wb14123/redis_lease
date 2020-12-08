package jepsen.redis.internal;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.ScriptOutputType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JepsenRedisLeaseClient implements JepsenRedisClient {

    private final RedisConnection<String, String> redisConnection;
    private final String getFileSha1;
    private final String setFileSha1;
    private final String delFileSha1;

    private String lease;

    public JepsenRedisLeaseClient(String hostname, String port, String scriptDirectory) {
        RedisURI redisURI = RedisURI.create("redis://" + hostname + ":" + port);
        RedisClient redisClient = new RedisClient(redisURI);
        redisConnection = redisClient.connect();
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
    public Optional<String> get(String key) {
        List<String> result = redisConnection.evalsha(getFileSha1, ScriptOutputType.MULTI,
                new String[]{key}, getUUID());
        lease = result.get(1);
        return Optional.ofNullable(result.get(0));
    }

    @Override
    public void set(String key, String value) {
        redisConnection.evalsha(setFileSha1, ScriptOutputType.VALUE, new String[]{key}, lease, value);
    }

    @Override
    public void del(String key) {
        redisConnection.evalsha(delFileSha1, ScriptOutputType.VALUE, key);
    }

    @Override
    public void close() {
        redisConnection.close();
    }
}

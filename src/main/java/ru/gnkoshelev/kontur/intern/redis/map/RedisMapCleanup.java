package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class RedisMapCleanup implements Runnable {
    private String redisKey;
    private JedisPool jedisPool;
    private AtomicBoolean isExecuted = new AtomicBoolean(false);
    private List<String> execKey;
    private List<String> params;

    public RedisMapCleanup(String redisKey, String changeCounterName, String subCountName, JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.redisKey = redisKey;
        execKey = new ArrayList<>();
        execKey.add("0");
        params = new ArrayList<>();
        params.add(subCountName);
        params.add(changeCounterName);
        params.add(redisKey);
    }


    @Override
    public void run() {
        if(isExecuted.compareAndSet(false, true)) {
            try (Jedis jedis = jedisPool.getResource()) {
                System.out.println(redisKey + " Cleanup");
                jedis.eval("local c = redis.call(\"decr\", ARGV[1]) if(c == 0) then redis.call(\"incr\", ARGV[2]) redis.call(\"del\", ARGV[3]) end return", execKey, params);
            }
        }
    }
}

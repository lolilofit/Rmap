package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.function.Consumer;

public class RedisEntryIterator extends RedisBasicIterator<Map.Entry<String, String>, RedisEntry> {
    public RedisEntryIterator(JedisPool jedisPool, String hmapName, MapParams mapParams) {
        super(jedisPool, hmapName, mapParams);
    }

    @Override
    public Map.Entry<String, String> next() {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();

        checkIsLast();
        Map.Entry<String, String> entry = redisPart.get(localCursor);
        lastElement = new RedisEntry(entry.getKey(), entry.getValue(), jedisPool, mapParams);
        localCursor++;
        return lastElement;
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<String, String>> action) {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();

        if (action == null)
            throw new NullPointerException();

        boolean shouldStop = false;

        while (!shouldStop) {
            for (int i = localCursor; i < redisPart.size(); i++) {
                Map.Entry<String, String> entry = redisPart.get(i);
                action.accept( new RedisEntry(entry.getKey(), entry.getValue(), jedisPool, mapParams));
            }
            localCursor = redisPart.size();

            if (!result.isCompleteIteration()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    result = jedis.hscan(hmapName, result.getCursor(), scanParams);
                }
                redisPart = result.getResult();
                localCursor = 0;
            } else
                shouldStop = true;
        }
    }
}

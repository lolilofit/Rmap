package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;


public class RedisKeyIterator extends RedisBasicIterator<String, String> {

    public RedisKeyIterator(JedisPool jedisPool, String hmapName, MapParams mapParams) {
        super(jedisPool, hmapName, mapParams);
    }

    @Override
    public String next() {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();

        checkIsLast();
        String resultKey = redisPart.get(localCursor).getKey();
        lastElement = resultKey;
        localCursor++;
        return resultKey;
    }

    @Override
    public void forEachRemaining(Consumer<? super String> action) {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();
        if(action == null)
            throw new NullPointerException();

        boolean shouldStop = false;

        while(!shouldStop) {
            for (int i = localCursor; i < redisPart.size(); i++) {
                action.accept(redisPart.get(i).getKey());
            }
            localCursor = redisPart.size();

            if(!result.isCompleteIteration()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    result = jedis.hscan(hmapName, result.getCursor(), scanParams);
                }
                redisPart = result.getResult();
                localCursor = 0;
            }
            else
                shouldStop = true;
        }
    }
}

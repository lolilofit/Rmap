package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class RedisValuesIterator extends RedisBasicIterator<String, String> {
    private String lastKey;

    public RedisValuesIterator(JedisPool jedisPool, String hmapName, MapParams mapParams) {
        super(jedisPool, hmapName, mapParams);
    }

    @Override
    public String next() {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();

        checkIsLast();
        Map.Entry<String, String> entry = redisPart.get(localCursor);
        lastKey = entry.getKey();
        lastElement = entry.getValue();
        localCursor++;
        return lastElement;
    }

    @Override
    public void remove() {
        if(!lastChanges.equals(mapParams.getChangeCounter()))
            throw new IllegalStateException();

        if(lastElement == null)
            throw new IllegalStateException();

        List<String> params = new ArrayList<>(mapParams.getBasicParams());
        params.add(lastKey);
        removeWithParams(params);
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
                action.accept(redisPart.get(i).getValue());
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

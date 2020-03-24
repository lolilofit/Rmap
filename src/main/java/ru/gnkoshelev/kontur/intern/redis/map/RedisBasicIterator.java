package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public abstract class RedisBasicIterator<T, V extends T> implements Iterator<T> {

    protected JedisPool jedisPool;
    protected String hmapName;
    protected ScanParams scanParams;
    protected ScanResult<Map.Entry<String, String>> result;
    protected List<Map.Entry<String, String>> redisPart;
    protected Integer localCursor = 0;
    protected MapParams mapParams;
    protected V lastElement;

    public RedisBasicIterator(JedisPool jedisPool, String hmapName, MapParams mapParams) {
        this.jedisPool = jedisPool;
        this.hmapName = hmapName;
        this.mapParams = mapParams;
        scanParams = new ScanParams();
        scanParams.count(10);
        lastElement = null;

        try(Jedis jedis = jedisPool.getResource()) {
           result = jedis.hscan(hmapName, "0", scanParams);
        }
        redisPart = result.getResult();
    }

    protected void checkIsLast() {
        if(localCursor == redisPart.size()) {
            if(result.isCompleteIteration())
                throw new NoSuchElementException();
            try (Jedis jedis = jedisPool.getResource()) {
                result = jedis.hscan(hmapName, result.getCursor(), scanParams);
            }
            redisPart = result.getResult();
        }
    }

    protected void removeWithParams(List<String> params) {
        Long oldChangeCounter = mapParams.getChangeCounter();
        Long resultCounter;
        try (Jedis jedis = jedisPool.getResource()) {
            resultCounter = UpdateChecker.checkUpdateWithRemove(jedis, params);
        }
        if(!oldChangeCounter.equals(resultCounter))
            throw new IllegalStateException();
        mapParams.setChangeCounter(oldChangeCounter + 1);
    }

    @Override
    public void remove() {
        if(lastElement == null)
            throw new IllegalStateException();
        List<String> params = new ArrayList<>(mapParams.getBasicParams());
        params.add(lastElement.toString());
        removeWithParams(params);
    }

    @Override
    public boolean hasNext() {
        if(redisPart.size() == 0)
            return false;
        if(localCursor == redisPart.size() && result.isCompleteIteration())
            return false;
        return true;
    }

}

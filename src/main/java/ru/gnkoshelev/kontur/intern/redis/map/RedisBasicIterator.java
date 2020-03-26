package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.*;

import java.util.*;
import java.util.List;


public abstract class RedisBasicIterator<T, V extends T> implements Iterator<T> {

    protected JedisPool jedisPool;
    protected String hmapName;
    protected ScanParams scanParams;
    protected ScanResult<Map.Entry<String, String>> result;
    protected List<Map.Entry<String, String>> redisPart;
    protected Integer localCursor = 0;
    protected MapParams mapParams;
    protected V lastElement;
    protected Long lastChanges;

    public RedisBasicIterator(JedisPool jedisPool, String hmapName, MapParams mapParams) {
        this.jedisPool = jedisPool;
        this.hmapName = hmapName;
        this.mapParams = mapParams;
        scanParams = new ScanParams();
        scanParams.count(10);
        lastElement = null;

        lastChanges = mapParams.getChangeCounter();
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
        Long oldChangeCounter = lastChanges;
        Long resultCounter;
        try (Jedis jedis = jedisPool.getResource()) {
            resultCounter = ScriptsStorage.checkUpdateWithRemove(jedis, params);
        }
        if(!oldChangeCounter.equals(resultCounter)) {
            mapParams.setChangeCounter(resultCounter);
            throw new IllegalStateException();
        }
        mapParams.setChangeCounter(oldChangeCounter + 1);
        lastChanges = mapParams.getChangeCounter();
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
        if(lastChanges != mapParams.getChangeCounter())
            throw new IllegalStateException();
        if(redisPart.size() == 0)
            return false;
        if(localCursor == redisPart.size() && result.isCompleteIteration())
            return false;
        return true;
    }

}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class RedisKeyIterator implements Iterator<String> {
    private WeakReference<LinkedHashSet<String>> savedKeySet;
    private Iterator<String> iterator;
    private JedisPool jedisPool;
    private List<String> keysParam;
    private MapParams mapParams;
    private String lastElement = null;

    public RedisKeyIterator(LinkedHashSet<String> set, JedisPool jedisPool, List<String> keysParam, MapParams mapParams) {
        this.savedKeySet = new WeakReference<>(set);
        this.jedisPool = jedisPool;
        this.keysParam = keysParam;
        this.mapParams = mapParams;
        iterator = Objects.requireNonNull(savedKeySet.get()).iterator();
    }

    @Override
    public boolean hasNext() {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet.get(), keysParam, mapParams.getBasicParams()));
        }
        return iterator.hasNext();
    }

    @Override
    public String next() {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet.get(), keysParam, mapParams.getBasicParams()));
        }
        lastElement =  iterator.next();
        return lastElement;
    }

    @Override
    public void remove() {
        List<Long> result;
        if(lastElement == null)
            throw  new IllegalStateException();

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(mapParams.getBasicParams());
            params.add(lastElement);
            result = UpdateChecker.checkUpdatesWithRemove(jedis, savedKeySet.get(), keysParam, params);
        }
        if(mapParams.getChangeCounter().equals(result.get(0))) {
            iterator.remove();
        }
        mapParams.setChangeCounter(result.get(0));
        lastElement = null;
    }

    @Override
    public void forEachRemaining(Consumer<? super String> action) {

    }

}

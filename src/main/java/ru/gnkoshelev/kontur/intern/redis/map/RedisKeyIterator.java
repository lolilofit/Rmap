package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.function.Consumer;

public class RedisKeyIterator implements Iterator<String> {
    private LinkedHashSet<String> savedKeySet;
    private Iterator<String> iterator;
    private JedisPool jedisPool;
    private List<String> basicParams;
    private List<String> keysParam;

    public RedisKeyIterator(Set<String> set, JedisPool jedisPool, List<String> basicParams, List<String> keysParam) {
        this.savedKeySet = new LinkedHashSet<>(set);
        this.jedisPool = jedisPool;
        this.basicParams = basicParams;
        this.keysParam = keysParam;
        iterator = savedKeySet.iterator();
    }

    @Override
    public boolean hasNext() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return iterator.hasNext();
    }

    @Override
    public String next() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return iterator.next();
    }

    @Override
    public void remove() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(basicParams);
            params.add(iterator.toString());
            UpdatedChecker.checkUpdatesWithRemove(jedis, savedKeySet, keysParam, params);
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super String> action) {

    }
}

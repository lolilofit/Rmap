package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RedisKeySet extends RedisBasicSet<String, String> {
    public RedisKeySet(JedisPool jedisPool, String hmapName, List<String> keysParam, MapParams mapParams) {
        super(jedisPool, hmapName, keysParam, mapParams);
    }

    @Override
    public Iterator<String> iterator() {
        return new RedisKeyIterator(jedisPool, hmapName, mapParams);
    }

    @Override
    public Object[] toArray() {
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        return keys.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        if(!ts.getClass().getComponentType().equals(String.class))
            throw new ArrayStoreException();
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        return keys.toArray(ts);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }
}

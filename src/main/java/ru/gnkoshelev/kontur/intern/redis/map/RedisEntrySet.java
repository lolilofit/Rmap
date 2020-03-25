package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.*;


public class RedisEntrySet extends RedisBasicSet<Map.Entry<String, String>, RedisEntry> {

    public RedisEntrySet(JedisPool jedisPool, MapParams mapParams) {
        super(jedisPool, mapParams);
    }

    private Set<RedisEntry> getAll() {
        Set<RedisEntry> set = new LinkedHashSet<>();
        Map<String, String> redisMap;
        try(Jedis jedis = jedisPool.getResource()) {
            redisMap = jedis.hgetAll(hmapName);
        }
        for(Map.Entry<String, String> entry : redisMap.entrySet()) {
            set.add(new RedisEntry(entry.getKey(), entry.getValue(), jedisPool, mapParams));
        }
        return set;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new RedisEntryIterator(jedisPool, hmapName, mapParams);
    }

    @Override
    public Object[] toArray() {
        Set<RedisEntry> set = getAll();
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        if(!Arrays.asList(ts.getClass().getComponentType().getInterfaces()).contains(Map.Entry.class))
            throw new ArrayStoreException();
        Set<RedisEntry> set = getAll();
        return set.toArray(ts);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }
}

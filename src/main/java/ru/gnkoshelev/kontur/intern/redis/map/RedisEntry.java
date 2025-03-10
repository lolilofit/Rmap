package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;

public class RedisEntry implements Map.Entry<String, String> {
    private String key;
    private String value;
    private JedisPool jedisPool;
    private MapParams mapParams;

    public RedisEntry(String key, String value, JedisPool jedisPool, MapParams mapParams) {
        this.key = key;
        this.value = value;
        this.jedisPool = jedisPool;
        this.mapParams = mapParams;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String s) {
        if(s == null)
            throw  new NullPointerException();

        String oldValue = value;
        this.value = s;
        try(Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hset(mapParams.getMapName(), key, value);
            transaction.incr(mapParams.getChangeCounterName());
            List<Object> result = transaction.exec();

            mapParams.setChangeCounter(Long.valueOf(result.get(1).toString()));
        }
        return oldValue;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;

        if(o instanceof Map.Entry) {
            Map.Entry<?, ?> entry = (Map.Entry)o;
            return entry.getValue().equals(value) && entry.getKey().equals(key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode();
    }
}

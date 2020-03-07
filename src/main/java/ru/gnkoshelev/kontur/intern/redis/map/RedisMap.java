package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Gregory Koshelev
 */
public class RedisMap implements Map<String,String> {
    private final String connectionIp = "127.0.0.1";
    private final int connectionPort = 6379;
    private JedisPool jedisPool;
    private String hmapName;
    private final String baseKeyName = "redis_map";

    //перенести из конструктора
    RedisMap() {
        jedisPool = new JedisPool(connectionIp, connectionPort);
        //зациклить
        try (Jedis jedis = jedisPool.getResource()) {
           Long number = jedis.incr("map_counter");
           hmapName = baseKeyName + ":" + number.toString();
        }
    }

    @Override
    public int size() {
        int size = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            size = Math.toIntExact(jedis.hlen(hmapName));
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        if(this.size() == 0)
            return true;
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean doesExists;
        try (Jedis jedis = jedisPool.getResource()) {
            doesExists = jedis.hexists(hmapName, key.toString());
        }
        return doesExists;
    }

    @Override
    public boolean containsValue(Object value) {
        Object result;
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> keys = new ArrayList<>(1);
            keys.add("0");
            List<String> params = new ArrayList<>(1);
            params.add(value.toString());
            //из файла+
            result = jedis.eval("local val = \"12\" local values = redis.call(\"HVALS\", \"user:1000\") for i, name in ipairs(values) do if name == val then return 1 end end return 0", keys, params);
        }
        if(result instanceof Integer) {
            Integer castedResult = (Integer)result;
            return castedResult == 1;
        }
        return false;
    }

    @Override
    public String get(Object key) {
        List<String> returnedValues;
        try (Jedis jedis = jedisPool.getResource()) {
             returnedValues = jedis.hmget(hmapName, key.toString());
        }
        if(returnedValues.size() > 0)
            return returnedValues.get(0);
        return null;
    }

    @Override
    public String put(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(hmapName, key, value);
        }
        return value;
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> values = m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Object::toString));
            jedis.hmset(hmapName, values);
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        return keys;
    }

    @Override
    public Collection<String> values() {
        List<String> values;
        try (Jedis jedis = jedisPool.getResource()) {
            values = jedis.hvals(hmapName);
        }
        return values;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Map<String, String> m;
        try (Jedis jedis = jedisPool.getResource()) {
            m = jedis.hgetAll(hmapName);
        }
        return m.entrySet();
    }
}

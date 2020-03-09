package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.ref.WeakReference;
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
    private final String baseKeyName = "redis_map:";
    private final String  counterBase = "change_counter:";
    private String changeCounterName;
    //многопоточность?
    private Set<String> keySet = null;
    private Long changeCounter;

    //перенести из конструктора
    RedisMap() {
        jedisPool = new JedisPool(connectionIp, connectionPort);
        //зациклить
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth("sOmE_sEcUrE_pAsS");
           Long number = jedis.incr("map_counter");
           //string builder
            changeCounterName = counterBase + number.toString();
           jedis.set(changeCounterName,  "0");
           hmapName = baseKeyName + number.toString();
        }
    }

    public void init() {}


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
            params.add(hmapName);
            //из файла+
            result = jedis.eval("local val = ARGV[1] local values = redis.call(\"HVALS\", ARGV[2]) for i, name in ipairs(values) do if name == val then return 1 end end return 0", keys, params);
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

    //what to return?
    @Override
    public String put(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(hmapName, key, value);
            changeCounter = jedis.incr(changeCounterName);
            if(keySet != null) {
                keySet.add(key);
            }

        }
        return value;
    }

    @Override
    public String remove(Object key) {
        //String value;
        try (Jedis jedis = jedisPool.getResource()) {
         //   value = jedis.hdel(hmapName, key.toString());
            changeCounter = jedis.incr(changeCounterName);
            if(keySet != null) {
                keySet.remove(key.toString());
            }
        }
       // return value;
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> values = m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Object::toString));
            jedis.hmset(hmapName, values);
            changeCounter = jedis.incr(changeCounterName);
            if(keySet != null)
                keySet.addAll(m.keySet());
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(hmapName);
            if(keySet != null)
                keySet.clear();
        }
    }

    @Override
    public Set<String> keySet() {
        if(keySet == null) {
            Set<String> keys;
            try (Jedis jedis = jedisPool.getResource()) {
                keys = jedis.hkeys(hmapName);
                keySet = new RedisSet(keys, jedisPool, hmapName, changeCounter, changeCounterName);
            }
        }
        return keySet;
    }

    //переписать
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
       /* Map<String, String> m;
        try (Jedis jedis = jedisPool.getResource()) {
            m = jedis.hgetAll(hmapName);
        }
        return m.entrySet();
        */
       return null;
    }
}

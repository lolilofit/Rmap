package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Gregory Koshelev
 */
public class RedisMap implements Map<String,String> {

    private final String connectionIp = "127.0.0.1";
    private final int connectionPort = 6379;
    private final String baseKeyName = "redis_map:";
    private final String  counterBase = "change_counter:";
    private final String password = "sOmE_sEcUrE_pAsS";
    private final String mapCounter = "map_counter";
    private final String subscribersCountBase = "sub_count:";

    private JedisPool jedisPool;
    private String hmapName;
    private String subCountName;
    private String changeCounterName;
    private MapParams mapParams;
    private List<String> keysParam;

    private void makeNewMap(Jedis jedis) {

        Long number = jedis.incr(mapCounter);
        changeCounterName = counterBase + number.toString();
        subCountName = subscribersCountBase + number.toString();
        jedis.set(changeCounterName,  "0");
        jedis.set(subCountName, "0");
        hmapName = baseKeyName + number.toString();
        mapParams = new MapParams(hmapName, changeCounterName, subCountName,0L);

    }

    private void registerCleaner() {
        RedisMapCleanup redisMapCleanup = new RedisMapCleanup(hmapName, changeCounterName, subCountName, jedisPool);
        RedisMapCleanerRegistrar.register(this, redisMapCleanup);
        Runtime.getRuntime().addShutdownHook(new Thread(redisMapCleanup));
    }

    RedisMap() {
        jedisPool = new JedisPool(connectionIp, connectionPort);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(password);
            makeNewMap(jedis);
        }

        registerCleaner();
        System.out.println(hmapName);
        keysParam = new ArrayList<>(1);
        keysParam.add("0");
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
        List<String> params = new ArrayList<>(1);
        params.add(value.toString());
        params.add(hmapName);

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getContainsValueScript(), mapParams.getExecKey(), params);
        }
        if(result instanceof Long)
            return (Long) result == 1;
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
        if(key == null || value == null)
            throw new NullPointerException();

        String previousValue = null;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hget(hmapName, key);
            transaction.hset(hmapName, key, value);
            transaction.incr(changeCounterName);
            List<Object> result = transaction.exec();

            mapParams.setChangeCounter(Long.valueOf(result.get(2).toString()));
            if(result.get(0) != null)
                previousValue = result.get(0).toString();
        }
        return previousValue;
    }

    @Override
    public String remove(Object key) {
        String prevValue = null;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hget(hmapName, key.toString());
            transaction.hdel(hmapName, key.toString());
            transaction.incr(changeCounterName);
            List<Object> result = transaction.exec();

            mapParams.setChangeCounter(Long.valueOf(result.get(2).toString()));
            if(result.get(0) != null)
                prevValue = result.get(0).toString();
        }
        return prevValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

        Map<String, String> values = m.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Object::toString));

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hmset(hmapName, values);
            transaction.incr(changeCounterName);
            List<Object> result = transaction.exec();

            mapParams.setChangeCounter(Long.valueOf(result.get(1).toString()));
        }
    }

    @Override
    public void clear() {
        List<String> params = new ArrayList<>();
        params.add(mapParams.getSubCounterName());
        params.add(mapParams.getChangeCounterName());
        params.add(mapParams.getMapName());
        Object res;

        try (Jedis jedis = jedisPool.getResource()) {
            res = jedis.eval("local c = redis.call(\"decr\", ARGV[1]) if(c == 0) then redis.call(\"del\", ARGV[3]) redis.call(\"incr\", ARGV[2]) end return -1", mapParams.getExecKey(), params);
        }
        if((Long)res > 0)
            mapParams.setChangeCounter((Long)res);
    }

    @Override
    public Set<String> keySet() {
        return new RedisKeySet(jedisPool, mapParams);
    }

    @Override
    public Collection<String> values() {
        return new RedisCollection(mapParams, jedisPool);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new RedisEntrySet(jedisPool, mapParams);
    }

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;
        if(o instanceof Map) {
            Map<String, String> redisMap;
            try (Jedis jedis = jedisPool.getResource()) {
                 redisMap = jedis.hgetAll(hmapName);
            }
            Map<?, ?> objMap = (Map)o;
            Iterator<Entry<String, String>> redisIterator = this.entrySet().iterator();
            Iterator<? extends Entry<?, ?>> oIterator = objMap.entrySet().iterator();
            if(objMap.size() == redisMap.size()) {
                if(redisIterator.hasNext()) {
                    if(!redisIterator.next().equals(oIterator.next()))
                        return false;
                }
                return true;
            }
            else
                return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hmapName.hashCode();
    }
}

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

    private static final String connectionIp = "127.0.0.1";
    private static final int connectionPort = 6379;
    private static final String baseKeyName = "redis_map:";
    private static final String counterBase = "change_counter:";
    private static final String password = "sOmE_sEcUrE_pAsS";
    private static final String mapCounter = "map_counter";
    private static final String subscribersCountBase = "sub_count:";

    private JedisPool jedisPool;
    private String hmapName;
    private String subCountName;
    private String changeCounterName;
    private MapParams mapParams;
    private List<String> keysParam;
    private String actualName;

    private void makeNewMap(Jedis jedis) {

        Long number = jedis.incr(mapCounter);
        actualName = number.toString();
        changeCounterName = counterBase + number.toString();
        subCountName = subscribersCountBase + number.toString();

        jedis.set(changeCounterName,  "0");
        jedis.set(subCountName, "1");

        hmapName = baseKeyName + number.toString();
        mapParams = new MapParams(hmapName, changeCounterName, subCountName,0L);

    }

    private void registerCleaner() {
        RedisMapCleanup redisMapCleanup = new RedisMapCleanup(hmapName, changeCounterName, subCountName, jedisPool);
        RedisMapCleanerRegistrar.register(this, redisMapCleanup);
        Runtime.getRuntime().addShutdownHook(new Thread(redisMapCleanup));
    }

    public RedisMap() {
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

    public RedisMap(String mapKey) {
        jedisPool = new JedisPool(connectionIp, connectionPort);
        changeCounterName = counterBase + mapKey;
        subCountName = subscribersCountBase + mapKey;
        hmapName = baseKeyName + mapKey;
        Long changesCounter;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(password);
            jedis.incr(subCountName);
            changesCounter = Long.valueOf(jedis.get(changeCounterName));
        }

        mapParams = new MapParams(hmapName, changeCounterName, subCountName,changesCounter);
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
        if(!(key instanceof String))
            return false;

        boolean doesExists;
        try (Jedis jedis = jedisPool.getResource()) {
            doesExists = jedis.hexists(hmapName, key.toString());
        }
        return doesExists;
    }

    @Override
    public boolean containsValue(Object value) {
        if(!(value instanceof String))
            return false;

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
        return returnedValues.get(0);
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
        if(!(key instanceof String))
            return null;

        List<String> params = new ArrayList<>();
        params.add(hmapName);
        params.add(key.toString());
        params.add(mapParams.getChangeCounterName());

        String prevValue = null;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.eval(ScriptsStorage.getTestAndIncrScript(), mapParams.getExecKey(), params);
            transaction.hget(hmapName, key.toString());
            transaction.hdel(hmapName, key.toString());
            List<Object> result = transaction.exec();

            if((Long)result.get(2) > 0)
                mapParams.setChangeCounter(mapParams.getChangeCounter() + 1);
            if(result.get(1) != null)
                prevValue = result.get(1).toString();
        }
        return prevValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        if(m == null)
            throw new NullPointerException();
        if(m.size() == 0)
            return;

        Map<String, String> values = m.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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
        List<Object> res;

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.incr(mapParams.getChangeCounterName());
            transaction.del(mapParams.getMapName());
            res= transaction.exec();
        }
        mapParams.setChangeCounter((Long)res.get(0));
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
        if(o == null)
            return false;

        if(o instanceof Map) {
            Map<String, String> redisMap;
            try (Jedis jedis = jedisPool.getResource()) {
                 redisMap = jedis.hgetAll(hmapName);
            }
            Map<?, ?> objMap = (Map)o;
            Iterator<Entry<String, String>> redisIterator = this.entrySet().iterator();
           // Set<? extends Entry<?, ?>> objSet = objMap.entrySet();
            Iterator<? extends Entry<?, ?>> oIterator = objMap.entrySet().iterator();

            if(objMap.size() == redisMap.size()) {
                if(redisIterator.hasNext()) {
                    if(!redisIterator.next().equals(oIterator.next()))
                  //  if(!objSet.contains(redisIterator.next()))
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
        int resultHash = 0;
        Map<String, String> redisMap;
        try (Jedis jedis = jedisPool.getResource()) {
            redisMap = jedis.hgetAll(hmapName);
        }
        for(Entry<String, String> entry : redisMap.entrySet()) {
            resultHash += entry.getKey().hashCode();
            resultHash += entry.getValue().hashCode();
        }
        return resultHash;
    }

    @Override
    public String toString() {
        return actualName;
    }
}

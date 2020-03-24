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

    private JedisPool jedisPool;
    private String hmapName;
    private String changeCounterName;
   // private RedisSet<String, String> keySet = null;
  //  private LinkedHashSet<String> keySetForAdding = null;
    private RedisMapCleanup cleaner;
    private MapParams mapParams;

  //  private RedisSet<Entry<String, String>, RedisEntry> entryRedisSet = null;
 //   private LinkedHashSet<RedisEntry> redisEntryForAdding = null;
  //  private UpdateChecker updateChecker;
    private List<String> keysParam;

    //перенести из конструктора
    RedisMap() {
        jedisPool = new JedisPool(connectionIp, connectionPort);
        //зациклить
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(password);
            //transaction??
            Long number = jedis.incr(mapCounter);
            changeCounterName = counterBase + number.toString();
            jedis.set(changeCounterName,  "0");
            hmapName = baseKeyName + number.toString();
            mapParams = new MapParams(hmapName, changeCounterName, 0L);

            RedisMapCleanup redisMapCleanup = new RedisMapCleanup(hmapName, jedisPool);
            cleaner = redisMapCleanup;
            RedisMapCleanerRegistrar.register(this, redisMapCleanup);
            Runtime.getRuntime().addShutdownHook(new Thread(redisMapCleanup));

            System.out.println(hmapName);
          //  entryRedisSet = null;
            keysParam = new ArrayList<>(1);
            keysParam.add("0");
         //   updateChecker = new UpdateChecker(jedisPool,mapParams, keysParam);
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

        //    if(keySet != null) {
       //         keySetForAdding.add(key);
       //     }
       //     mapParams.setChangeCounter(Long.valueOf(result.get(2).toString()));
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

          //  mapParams.setChangeCounter(Long.valueOf(result.get(2).toString()));
            if(result.get(0) != null)
                prevValue = result.get(0).toString();
      //      if(keySet != null) {
      //          keySetForAdding.remove(key.toString());
      //      }
        }
        return prevValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        try (Jedis jedis = jedisPool.getResource()) {

            Map<String, String> values = m.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Object::toString));

            Transaction transaction = jedis.multi();
            transaction.hmset(hmapName, values);
    //        transaction.incr(changeCounterName);
            List<Object> result = transaction.exec();

     //       mapParams.setChangeCounter(Long.valueOf(result.get(1).toString()));
   //         if(keySet != null)
    //           keySetForAdding.addAll(m.keySet());
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
        //    Transaction transaction = jedis.multi();
            jedis.del(hmapName);
      //      transaction.incr(changeCounterName);
       //     List<Object> result = transaction.exec();

      //      mapParams.setChangeCounter(Long.valueOf(result.get(1).toString()));
     //       if(keySet != null)
     //           keySetForAdding.clear();
        }
    }

    @Override
    public Set<String> keySet() {
        return new RedisSet<String,String>(jedisPool, hmapName, keysParam);
        /*

        if(keySet == null) {
            try (Jedis jedis = jedisPool.getResource()) {
                keySetForAdding = new LinkedHashSet<>(jedis.hkeys(hmapName));
                updateChecker.setKeySetForAdding(keySetForAdding);
                keySet = new RedisSet<>(keySetForAdding, String.class, jedisPool, hmapName, mapParams, keysParam, updateChecker);
            }

            if(cleaner != null)
                cleaner.setKeySet(keySetForAdding);
        }
        return keySet;
         */
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
        return new RedisSet<Entry<String, String>, RedisEntry>(jedisPool, hmapName, keysParam);
        /*
       if(entryRedisSet == null) {
           redisEntryForAdding = new LinkedHashSet<>();
           Map<String, String> redisMap;
           try(Jedis jedis = jedisPool.getResource()) {
               redisMap = jedis.hgetAll(hmapName);
           }
           for(Map.Entry<String,String>entry : redisMap.entrySet())
               redisEntryForAdding.add(new RedisEntry(entry.getKey(), entry.getValue(), jedisPool, mapParams));

           updateChecker.setRedisEntryForAdding(redisEntryForAdding);
           entryRedisSet = new RedisSet<>(redisEntryForAdding, RedisEntry.class, jedisPool, hmapName, mapParams, keysParam, updateChecker);
       }
       if(cleaner != null)
           cleaner.setEntrySet(redisEntryForAdding);
       return entryRedisSet;

         */
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
            return redisMap.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hmapName.hashCode();
    }
}

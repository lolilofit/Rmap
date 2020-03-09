package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

public class RedisSet implements Set<String> {
    private LinkedHashSet<String> savedKeySet;
    private JedisPool jedisPool;
    private String hmapName;
    private Long changeCounter;
    private String changeCounterName;
    private List<String> keysParam;
    List<String> basicParams;


    public RedisSet(Set<String> set, JedisPool jedisPool, String hmapName, Long changeCounter, String changeCounterName) {
        this.hmapName = hmapName;
        this.jedisPool = jedisPool;
        this.changeCounter = changeCounter;
        this.changeCounterName = changeCounterName;
        savedKeySet = new LinkedHashSet<>(set);
        keysParam = new ArrayList<>(1);
        keysParam.add("0");
        basicParams = new ArrayList<>(4);
        basicParams.add(changeCounter.toString());
        basicParams.add(changeCounterName);
        basicParams.add(hmapName);
    }


    @Override
    public boolean remove(Object o) {

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(4);
            params.add(changeCounter.toString());
            params.add(changeCounterName);
            params.add(hmapName);
            params.add(o.toString());
            Object values = jedis.eval("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) " +
                    "redis.call(\"hdel\", ARGV[3], ARGV[4]) " +
                    "if(change_counter ~= current_number) then " +
                    "return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  " +
                    "return {change_counter, nil}", keysParam, params);
            Collection castedValues = (Collection) values;
            Iterator iterator = castedValues.iterator();
            changeCounter = (Long) iterator.next();
            if(castedValues.size() > 1) {
                Set<String> currentKeySet = (Set<String>) iterator.next();
                savedKeySet.clear();
                savedKeySet.addAll(currentKeySet);
                return true;
            }
            else return savedKeySet.remove(o);
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(4);
            params.add(changeCounter.toString());
            params.add(changeCounterName);
            params.add(hmapName);
            Iterator<?> paramsIteratior = collection.iterator();
            while(paramsIteratior.hasNext()) {
                params.add(paramsIteratior.next().toString());
            }

            Object values = jedis.eval("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) " +
                    "for i = 4, #ARGV, 1 do redis.call(\"hdel\", ARGV[3], ARGV[i]) end" +
                    "if(change_counter ~= current_number) then " +
                    "return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  " +
                    "return {change_counter, nil}", keysParam, params);
            Collection castedValues = (Collection) values;
            Iterator iterator = castedValues.iterator();
            changeCounter = (Long) iterator.next();
            if(castedValues.size() > 1) {
                Set<String> currentKeySet = (Set<String>) iterator.next();
                savedKeySet.clear();
                savedKeySet.addAll(currentKeySet);
                return true;
            }
            else {
                //???
                boolean result = true;
                paramsIteratior = collection.iterator();
                while(paramsIteratior.hasNext()) {
                    result = result & savedKeySet.remove(paramsIteratior.next());
                }
                return result;
            }
        }
    }

    @Override
    public boolean add(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends String> collection) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(hmapName);
            savedKeySet.clear();
        }
    }

    @Override
    public int size() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.size();
    }

    @Override
    public boolean isEmpty() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.iterator();
    }

    @Override
    public Object[] toArray() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.toArray(ts);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.containsAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.retainAll(collection);
    }

    @Override
    public boolean equals(Object o) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.equals(o);
    }

    @Override
    public int hashCode() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdatedChecker.checkForUpdates(jedis, savedKeySet, keysParam, basicParams);
        }
        return savedKeySet.hashCode();
    }
}

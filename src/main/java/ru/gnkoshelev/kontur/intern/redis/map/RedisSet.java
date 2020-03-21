package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.*;

public class RedisSet implements Set<String> {
    private LinkedHashSet<String> savedKeySet;
    private JedisPool jedisPool;
    private String hmapName;
   // private Long changeCounter;
    private List<String> keysParam;
   // List<String> basicParams;
    private MapParams mapParams;


    public RedisSet(LinkedHashSet<String> set, JedisPool jedisPool, String hmapName, MapParams mapParams) {
        this.hmapName = hmapName;
        this.jedisPool = jedisPool;
        this.mapParams = mapParams;

        savedKeySet = set;
        keysParam = new ArrayList<>(1);
        keysParam.add("0");
        /*
        this.changeCounter = changeCounter;
        basicParams = new ArrayList<>(4);
        basicParams.add(changeCounter.toString());
        basicParams.add(changeCounterName);
        basicParams.add(hmapName);
        */
    }

    @Override
    public boolean remove(Object o) {
        List<Long> result;
        Long oldChangeCounter = mapParams.getChangeCounter();

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(4);
            //params.add(changeCounter.toString());
            //params.add(changeCounterName);
            //params.add(hmapName);

            params.addAll(mapParams.getBasicParams());
            params.add(o.toString());
            result = UpdateChecker.checkUpdatesWithRemove(jedis, savedKeySet, keysParam, params);
            /*
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
             */
        }

        mapParams.setChangeCounter(result.get(0));
        if(mapParams.getChangeCounter().equals(oldChangeCounter))
            savedKeySet.remove(o);
        return (result.get(1) > 0);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        List<Long> result;
        Long oldChangeCounter = mapParams.getChangeCounter();

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(4);
            /*params.add(changeCounter.toString());
            params.add(changeCounterName);
            params.add(hmapName);
            Iterator<?> paramsIteratior = collection.iterator();
            while(paramsIteratior.hasNext()) {
                params.add(paramsIteratior.next().toString());
            }
             */
            params.addAll(mapParams.getBasicParams());
            Iterator<?> paramsIteratior = collection.iterator();
            while(paramsIteratior.hasNext()) {
                params.add(paramsIteratior.next().toString());
            }
            result = UpdateChecker.checkUpdateWithRemoveAll(jedis, savedKeySet, keysParam, params);
/*
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
 */
        }
        mapParams.setChangeCounter(result.get(0));
        if(mapParams.getChangeCounter().equals(oldChangeCounter)) {
            for (Object o : collection) {
                savedKeySet.remove(o.toString());
            }
        }
        return result.get(1) > 1;
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
            Transaction transaction = jedis.multi();
            transaction.del(hmapName);
            transaction.incr(mapParams.getChangeCounterName());
            transaction.exec();
            savedKeySet.clear();
        }
    }

    @Override
    public int size() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams());
        }
        return savedKeySet.size();
    }

    @Override
    public boolean isEmpty() {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams());
        }
        return savedKeySet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        try (Jedis jedis = jedisPool.getResource()) {
            UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams());
        }
        return savedKeySet.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        return new RedisKeyIterator(savedKeySet, jedisPool, keysParam, mapParams);
    }

    @Override
    public Object[] toArray() {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        return savedKeySet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        if(ts == null)
            throw new NullPointerException();
        if(!ts.getClass().getComponentType().equals(String.class))
            throw  new ArrayStoreException();
       return savedKeySet.toArray(ts);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        return savedKeySet.containsAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        return savedKeySet.retainAll(collection);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Set) {
            try (Jedis jedis = jedisPool.getResource()) {
                mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
            }
            return savedKeySet.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(UpdateChecker.checkForUpdates(jedis, savedKeySet, keysParam, mapParams.getBasicParams()));
        }
        return savedKeySet.hashCode();
    }

}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.util.*;

public abstract class RedisBasicSet<T, V extends T> implements Set<T> {
    protected JedisPool jedisPool;
    protected String hmapName;
    protected List<String> keysParam;
    protected MapParams mapParams;


    public RedisBasicSet(JedisPool jedisPool, String hmapName, List<String> keysParam, MapParams mapParams) {
        this.hmapName = hmapName;
        this.jedisPool = jedisPool;
        this.keysParam = keysParam;
        this.mapParams = mapParams;
    }

    @Override
    public boolean remove(Object o) {
        Long changedCount;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hdel(hmapName, o.toString());
            transaction.incr(mapParams.getChangeCounterName());
            List<Object> result = transaction.exec();
            changedCount = (Long) result.get(0);
            mapParams.setChangeCounter((Long) result.get(1));
        }
        return changedCount > 0;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        List<Object> result;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.incr(mapParams.getChangeCounterName());
            for (Object o : collection)
                transaction.hdel(hmapName, o.toString());
            result = transaction.exec();
        }
        mapParams.setChangeCounter((Long) result.get(0));
        for(int i = 1; i < result.size(); i++) {
            if((Integer)result.get(i) > 0)
                return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if(collection == null)
            throw new NullPointerException();

        boolean answer = true;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            Iterator<?> iter = collection.iterator();
            while (iter.hasNext()) {
                Object value = iter.next();
                if(value == null) {
                    transaction.exec();
                    return false;
                }
                transaction.hexists(hmapName, value.toString());
            }
            List<Object> result = transaction.exec();
            for(int i = 0; i < result.size(); i++)
                answer = answer && (Boolean)result.get(i);
        }
        return answer;
    }

    @Override
    public void clear() {
        List<String> execKey = new ArrayList<>();
        execKey.add("0");
        List<String> params = new ArrayList<>();
        params.add(mapParams.getSubCounterName());
        params.add(mapParams.getChangeCounterName());
        params.add(mapParams.getMapName());
        Object res;
        try (Jedis jedis = jedisPool.getResource()) {
            res = jedis.eval("local c = redis.call(\"decr\", ARGV[1]) if(c == 0) then redis.call(\"del\", ARGV[3]) redis.call(\"incr\", ARGV[2]) end return -1", execKey, params);
        }
        if((Long)res > 0)
            mapParams.setChangeCounter((Long)res);
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
    public boolean contains(Object o) {
        boolean doesExists;
        try (Jedis jedis = jedisPool.getResource()) {
            doesExists = jedis.hexists(hmapName, o.toString());
        }
        return doesExists;
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }
/*
    @Override
    public boolean retainAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return savedLocalSet.retainAll(collection);
    }
*/
    @Override
    public boolean equals(Object o) {
        if(o instanceof Set) {
            try (Jedis jedis = jedisPool.getResource()) {
                Set<String> keys = jedis.hkeys(hmapName);
                return o.equals(keys);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return  hmapName.hashCode();
    }

}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.util.*;


public abstract class RedisBasicSet<T, V extends T> implements Set<T> {
    protected JedisPool jedisPool;
    protected String hmapName;
    protected MapParams mapParams;

    public RedisBasicSet(JedisPool jedisPool, MapParams mapParams) {
        this.hmapName = mapParams.getMapName();
        this.jedisPool = jedisPool;
        this.mapParams = mapParams;
    }

    private String getKeyToRemove(Object o) {
        if(o instanceof String)
            return (String)o;
        if(o instanceof Map.Entry)
            return ((Map.Entry<?, ?>)o).getKey().toString();
        return null;
    }

    @Override
    public boolean remove(Object o) {
        Long changedCount;
        String key = getKeyToRemove(o);
        if(key == null)
            return false;

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.hdel(hmapName, key);
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
            if((Long)result.get(i) > 0)
                return true;
        }
        return false;
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
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    protected abstract Set<Object> getAll();

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;

        if(o instanceof Set) {
            //Set<String> keys;
            //try (Jedis jedis = jedisPool.getResource()) {
            //   keys = jedis.hkeys(hmapName);
           // }
            Set<Object> set = this.getAll();
            if(set == null)
                return false;

            Set<?> objSet = (Set<?>) o;
            if(set.size() == objSet.size()) {
                Iterator<?> objIterator = objSet.iterator();
                for (Object element : set) {
                    Object objValue = objIterator.next();
                    if(objValue == null)
                        return false;

                    if (!objSet.contains(element))
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
        int resultCode = 0;
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        for(String key : keys) {
            resultCode += key.hashCode();
        }
        return resultCode;
    }
}

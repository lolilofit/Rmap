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
        return o.toString();
    }

    @Override
    public boolean remove(Object o) {
        if(o == null)
            throw new NullPointerException();

        List<Object> result;
        String key = getKeyToRemove(o);
        List<String> params = new ArrayList<>();
        params.add(hmapName);
        params.add(key);
        params.add(mapParams.getChangeCounterName());

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.eval(ScriptsStorage.getTestAndIncrScript(), mapParams.getExecKey(), params);
            transaction.hdel(hmapName, key);
            result = transaction.exec();
        }
        if((Long)result.get(1) > 0)
            mapParams.setChangeCounter(mapParams.getChangeCounter() + 1);

        return (Long)result.get(1) > 0;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        if(collection == null)
            throw new NullPointerException();

        List<Object> result;
        List<String> params = new ArrayList<>();
        params.add(hmapName);
        params.add("");
        params.add(mapParams.getChangeCounterName());
        int notNullCnt = 0;

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            for (Object o : collection) {
                if(o != null) {
                    notNullCnt++;
                    params.set(1, getKeyToRemove(o));
                    transaction.eval(ScriptsStorage.getTestAndIncrScript(), mapParams.getExecKey(), params);
                    //transaction.incr(mapParams.getChangeCounterName());
                }
            }
            for (Object o : collection) {
                if(o != null) {
                    String val = getKeyToRemove(o);
                    transaction.hdel(hmapName, val);
                }
            }
            result = transaction.exec();
        }
        //mapParams.setChangeCounter((Long) result.get(0));
        for(int i = notNullCnt; i < 2*notNullCnt; i++) {
            if((Long)result.get(i) > 0) {
                mapParams.setChangeCounter(mapParams.getChangeCounter() + 1);
                return true;
            }
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
    public boolean retainAll(Collection<?> collection) {
        if(collection == null)
            throw new NullPointerException();

        Object result;
        List<String> params = new ArrayList<>();
        params.add(hmapName);
        params.add(mapParams.getChangeCounterName());

        for (Object o : collection) {
            if(o != null) {
                params.add(getKeyToRemove(o));
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getRetainAllKeyScript(), mapParams.getExecKey(), params);
        }
        if((Long)result > 0) {
            mapParams.setChangeCounter((Long) result);
            return true;
        }
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

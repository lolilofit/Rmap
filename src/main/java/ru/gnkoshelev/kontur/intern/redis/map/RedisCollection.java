package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.*;

public class RedisCollection implements Collection<String> {
    private MapParams mapParams;
    private JedisPool jedisPool;
    private String hmapName;

    public RedisCollection(MapParams mapParams, JedisPool jedisPool) {
        this.mapParams = mapParams;
        this.jedisPool = jedisPool;
        this.hmapName = mapParams.getMapName();
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
        Object result;
        List<String> params = new ArrayList<>(1);
        params.add(o.toString());
        params.add(hmapName);

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getContainsValueScript(), mapParams.getExecKey(), params);
        }

        if(result instanceof Long) {
            Long castedResult = (Long) result;
            return castedResult == 1;
        }
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        return new RedisValuesIterator(jedisPool, hmapName, mapParams);
    }

    @Override
    public Object[] toArray() {
        Collection<String> values;
        try (Jedis jedis = jedisPool.getResource()) {
            values = jedis.hvals(hmapName);
        }
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {

        if(!ts.getClass().getComponentType().equals(String.class))
            throw new ArrayStoreException();

        Arrays.fill(ts, null);

        if(!ts.getClass().getComponentType().equals(String.class))
            throw new ArrayStoreException();
        Collection<String> values;
        try (Jedis jedis = jedisPool.getResource()) {
            values = jedis.hvals(hmapName);
        }
        return values.toArray(ts);
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
    public boolean remove(Object o) {
        Object result;
        List<String> params = new ArrayList<>(1);
        params.add(o.toString());
        params.add(hmapName);

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getRemoveByValueScript(), mapParams.getExecKey(), params);
        }
        if((Long)result == 0)
            return false;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Object result;
        List<String> params = new ArrayList<>(1);
        params.add(hmapName);
        for (Object o : collection)
            params.add(o.toString());

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getRemoveByCollectionScript(), mapParams.getExecKey(), params);
        }
        return (Long)result > 0;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if(collection == null)
            throw  new NullPointerException();

        List<Object> result;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            for (Object o : collection) {
                List<String> params = new ArrayList<>(1);
                params.add(o.toString());
                params.add(hmapName);
                transaction.eval(ScriptsStorage.getContainsValueScript(), mapParams.getExecKey(), params);
            }
            result = transaction.exec();
        }
        for (Object o : result) {
            if ((Long) o == 0)
                return false;
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        if(collection == null)
            throw new NullPointerException();

        Object result;
        List<String> params = new ArrayList<>(2);
        params.add(hmapName);
        params.add(mapParams.getChangeCounterName());

        for (Object o : collection) {
            if(o != null) {
                params.add(o.toString());
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.eval(ScriptsStorage.getRetainAllValueScript(), mapParams.getExecKey(), params);
        }

        if((Long)result > 0) {
            mapParams.setChangeCounter((Long) result);
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
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof Collection))
            return false;
        Collection<?> collection = (Collection<?>)o;
        Collection<String> values;
        try (Jedis jedis = jedisPool.getResource()) {
            values = jedis.hvals(hmapName);
        }
        if(values.size() != collection.size())
            return false;
        Iterator<?> collectionIterator = collection.iterator();
        for(String value : values) {
            Object collectionElement = collectionIterator.next();
            if(collectionElement == null)
                return false;

            if(!value.equals(collectionElement))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int resultHash = 0;
        Collection<String> values;
        try (Jedis jedis = jedisPool.getResource()) {
            values = jedis.hvals(hmapName);
        }
        for(String value : values)
            resultHash += value.hashCode();
        return resultHash;
    }
}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.*;


public class RedisKeySet extends RedisBasicSet<String, String> {
    public RedisKeySet(JedisPool jedisPool, MapParams mapParams) {
        super(jedisPool, mapParams);
    }

    @Override
    public boolean contains(Object o) {
        if(!(o instanceof String))
            return false;
        boolean doesExists;
        try (Jedis jedis = jedisPool.getResource()) {
            doesExists = jedis.hexists(hmapName, o.toString());
        }
        return doesExists;
    }

    @Override
    public Iterator<String> iterator() {
        return new RedisKeyIterator(jedisPool, hmapName, mapParams);
    }

    @Override
    public Object[] toArray() {
        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        return keys.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        if(!ts.getClass().getComponentType().equals(String.class))
            throw new ArrayStoreException();
        Arrays.fill(ts, null);

        Set<String> keys;
        try (Jedis jedis = jedisPool.getResource()) {
            keys = jedis.hkeys(hmapName);
        }
        return keys.toArray(ts);
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

    protected Set<Object> getAll() {
        Set<String> set;
        try (Jedis jedis = jedisPool.getResource()) {
            set = jedis.hkeys(hmapName);
        }
        return new LinkedHashSet<>(set);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }
}

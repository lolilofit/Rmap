package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.*;


public class RedisEntrySet extends RedisBasicSet<Map.Entry<String, String>, RedisEntry> {

    public RedisEntrySet(JedisPool jedisPool, MapParams mapParams) {
        super(jedisPool, mapParams);
    }


    protected Set<Object> getAll() {
        Set<RedisEntry> set = new LinkedHashSet<>();
        Map<String, String> redisMap;
        try(Jedis jedis = jedisPool.getResource()) {
            redisMap = jedis.hgetAll(hmapName);
        }
        for(Map.Entry<String, String> entry : redisMap.entrySet()) {
            set.add(new RedisEntry(entry.getKey(), entry.getValue(), jedisPool, mapParams));
        }
        return new LinkedHashSet<>(set);
    }

    @Override
    public boolean contains(Object o) {
        if(!(o instanceof Map.Entry))
            return false;
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
        String result;
        try(Jedis jedis = jedisPool.getResource()) {
            result = jedis.hget(hmapName, entry.getKey().toString());
        }
        if(result == null)
            return false;
        return result.equals(entry.getValue());
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new RedisEntryIterator(jedisPool, hmapName, mapParams);
    }

    @Override
    public Object[] toArray() {
        Set<Object> set = getAll();
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        Arrays.fill(ts, null);

        if(!Arrays.asList(ts.getClass().getComponentType().getInterfaces()).contains(Map.Entry.class) && !ts.getClass().getComponentType().equals(Map.Entry.class))
            throw new ArrayStoreException();
        Set<Object> set = getAll();
        return set.toArray(ts);
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
                if(!(value instanceof Map.Entry)) {
                    transaction.exec();
                    return false;
                }
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>)value;
                transaction.hget(hmapName, entry.getKey().toString());
            }
            List<Object> result = transaction.exec();
            int i = 0;
            for(Object o : collection) {
                if(!(o instanceof Map.Entry))
                    return false;
                if(result.get(i) == null)
                    return false;
                answer = answer && result.get(i).equals(((Map.Entry<?, ?>) o).getValue());
                i++;
            }
        }
        return answer;
    }
}

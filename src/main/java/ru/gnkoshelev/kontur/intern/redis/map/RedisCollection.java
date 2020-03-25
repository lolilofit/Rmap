package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
            result = jedis.eval("local val = ARGV[1] local values = redis.call(\"HVALS\", ARGV[2]) for i, name in ipairs(values) do if name == val then return 1 end end return 0", mapParams.getExecKey(), params);
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
            result = jedis.eval("local map = redis.call(\"HGETALL\", ARGV[2]) local val = ARGV[1] local key for i, v in ipairs(map) do if i % 2 == 1 then key = v else if(v == val) then return redis.call(\"HDEL\", ARGV[2], key) end end end return 0", mapParams.getExecKey(), params);
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
            result = jedis.eval("local map = redis.call(\"HGETALL\", ARGV[1])  local key for i, v in ipairs(map) do if i % 2 == 1 then key = v else for j = 1, #ARGV, 1 do if(v == ARGV[j]) then return redis.call(\"HDEL\", ARGV[2], key) end end end end return 0", mapParams.getExecKey(), params);
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
                transaction.eval("local val = ARGV[1] local values = redis.call(\"HVALS\", ARGV[2]) for i, name in ipairs(values) do if name == val then return 1 end end return 0", mapParams.getExecKey(), params);
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
        return false;
    }

    @Override
    public void clear() {
        List<String> params = new ArrayList<>();
        params.add(mapParams.getSubCounterName());
        params.add(mapParams.getChangeCounterName());
        params.add(mapParams.getMapName());
        Object res;
        try (Jedis jedis = jedisPool.getResource()) {
            res = jedis.eval("local c = redis.call(\"decr\", ARGV[1]) if(c == 0) then redis.call(\"del\", ARGV[3]) redis.call(\"incr\", ARGV[2]) end return -1", mapParams.getExecKey(), params);
        }
        if((Long)res > 0)
            mapParams.setChangeCounter((Long)res);
    }
}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.util.*;

public class RedisSet<T, V extends T> implements Set<T> {
   // private LinkedHashSet<V> savedLocalSet;
    private Class<V> componentType;
    private JedisPool jedisPool;
    private String hmapName;
    private List<String> keysParam;
 //   private MapParams mapParams;
 //   private UpdateChecker updateChecker;

    public RedisSet(JedisPool jedisPool, String hmapName, List<String> keysParam) {
        this.hmapName = hmapName;
        this.jedisPool = jedisPool;
    //    this.mapParams = mapParams;
        this.keysParam = keysParam;
   //     this.updateChecker = updateChecker;
      //  this.componentType = componentType;
       // savedLocalSet = set;
    }


    @Override
    public boolean remove(Object o) {
        Long changedCount;
        try (Jedis jedis = jedisPool.getResource()) {
            changedCount = jedis.hdel(hmapName, o.toString());
        }
        return changedCount > 0;
        /*
        List<Long> result;
        Long oldChangeCounter = mapParams.getChangeCounter();

        List<String> params = new ArrayList<>(4);
        params.addAll(mapParams.getBasicParams());
        params.add(o.toString());

        try (Jedis jedis = jedisPool.getResource()) {
            result = updateChecker.checkUpdatesWithRemove(jedis, params, o.toString());
        }

        mapParams.setChangeCounter(result.get(0));
        //if(mapParams.getChangeCounter().equals(oldChangeCounter))
        //    savedLocalSet.remove(o);

        return (result.get(1) > 0);

         */
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        List<Object> result;
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            for (Object o : collection)
                transaction.hdel(hmapName, o.toString());
            result = transaction.exec();
        }
        for(Object res : result) {
            if((Integer)res > 0)
                return true;
        }
        return false;
        /*
        List<Long> result;
        //Long oldChangeCounter = mapParams.getChangeCounter();

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(4);
            params.addAll(mapParams.getBasicParams());

            Set<String> objectsToRemove = new LinkedHashSet<>();
            Iterator<?> paramsIteratior = collection.iterator();
            while(paramsIteratior.hasNext()) {
                //check
                String value = paramsIteratior.next().toString();
                params.add(value);
                objectsToRemove.add(value);
            }

            result = updateChecker.checkUpdateWithRemoveAll(jedis, params, objectsToRemove);
        }
        mapParams.setChangeCounter(result.get(0));
       /*
        if(mapParams.getChangeCounter().equals(oldChangeCounter)) {
            for (Object o : collection) {
                if(componentType.equals(String.class) && o instanceof String)
                    savedLocalSet.remove(o);
            }
        }


        return result.get(1) > 1;
        */
    }


    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(hmapName);
            /*
            Transaction transaction = jedis.multi();
            transaction.del(hmapName);
            transaction.incr(mapParams.getChangeCounterName());
            transaction.exec();
            savedLocalSet.clear();

             */
        }
    }

    @Override
    public int size() {
        int size = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            size = Math.toIntExact(jedis.hlen(hmapName));
        }
        return size;
            /*
            updateChecker.checkForUpdates(jedis, mapParams.getBasicParams());
        }
        return savedLocalSet.size();
             */
    }

    @Override
    public boolean isEmpty() {
        if(this.size() == 0)
            return true;
        return false;
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            updateChecker.checkForUpdates(jedis, mapParams.getBasicParams());
        }
        return savedLocalSet.isEmpty();

         */
    }

    @Override
    public boolean contains(Object o) {
        boolean doesExists;
        try (Jedis jedis = jedisPool.getResource()) {
            doesExists = jedis.hexists(hmapName, o.toString());
        }
        return doesExists;
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            updateChecker.checkForUpdates(jedis, mapParams.getBasicParams());
        }
        return savedLocalSet.contains(o);

         */
    }

    @Override
    public Iterator<T> iterator() {
        return new RedisKeyIterator<T, V>(jedisPool, hmapName);
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return new RedisKeyIterator<T, V>(savedLocalSet, componentType, jedisPool, keysParam, mapParams, updateChecker);

         */
    }

    @Override
    public Object[] toArray() {
        if(componentType.equals(String.class)) {
            Set<String> keys;
            try (Jedis jedis = jedisPool.getResource()) {
                keys = jedis.hkeys(hmapName);
            }
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return savedLocalSet.toArray();
         */
            return keys.toArray();
        }
        if(Arrays.asList(componentType.getInterfaces()).contains(Map.Entry.class))  {
            Set<RedisEntry> entrySet;
        }
        return new Object[0];
    }

    @Override
    public <V> V[] toArray(V[] ts) {
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        if(ts == null)
            throw new NullPointerException();
        if(!ts.getClass().getComponentType().equals(savedLocalSet.getClass().getComponentType()))
            throw  new ArrayStoreException();
       return savedLocalSet.toArray(ts);

         */
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return savedLocalSet.containsAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return savedLocalSet.retainAll(collection);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Set) {
            try (Jedis jedis = jedisPool.getResource()) {
                Set<String> keys = jedis.hkeys(hmapName);
                return o.equals(keys);
            }
        }
        return false;
        /*
        if(o instanceof Set) {
            try (Jedis jedis = jedisPool.getResource()) {
                mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
            }
            return savedLocalSet.equals(o);
        }
        return false;
         */
    }

    @Override
    public int hashCode() {
        return  hmapName.hashCode();
    }

}

package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.function.Consumer;

public class RedisKeyIterator<T> implements Iterator<T> {
    //weak?
   // private WeakReference<LinkedHashSet<V>> savedKeySet;
 //   private Class<V> componentType;
 //   private Iterator<V> iterator;
//    private List<String> keysParam;
 //   private MapParams mapParams;
 //   private UpdateChecker updateChecker;
    private JedisPool jedisPool;
    private T lastElement = null;
    private String hmapName;
    private ScanParams scanParams;
    private  ScanResult<Map.Entry<String, String>> result;

    public RedisKeyIterator(JedisPool jedisPool, String hmapName) {
    //    this.savedKeySet = new WeakReference<>(set);
   //     this.componentType = componentType;
    //    this.keysParam = keysParam;
     //   this.mapParams = mapParams;
    //    this.updateChecker = updateChecker;
   //     iterator = Objects.requireNonNull(savedKeySet.get()).iterator();
        this.jedisPool = jedisPool;
        this.hmapName = hmapName;
        scanParams = new ScanParams();
        scanParams.count(10);

        try(Jedis jedis = jedisPool.getResource()) {
           result = jedis.hscan(hmapName, "0", scanParams);
        }
    }

    @Override
    public boolean hasNext() {
      /*  try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        return iterator.hasNext();

       */
    }

    @Override
    public T next() {
        /*
        try (Jedis jedis = jedisPool.getResource()) {
            mapParams.setChangeCounter(updateChecker.checkForUpdates(jedis, mapParams.getBasicParams()));
        }
        lastElement =  iterator.next();
        return lastElement;

         */
    }

    @Override
    public void remove() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(hmapName, lastElement.toString());
        }
        /*
        List<Long> result;
        if(lastElement == null)
            throw  new IllegalStateException();

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> params = new ArrayList<>(mapParams.getBasicParams());
            params.add(lastElement.toString());
            result = updateChecker.checkUpdatesWithRemove(jedis, params, lastElement.toString());
        }
        if(mapParams.getChangeCounter().equals(result.get(0))) {
            iterator.remove();
        }
        mapParams.setChangeCounter(result.get(0));
        lastElement = null;

         */
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if(action == null)
            throw new NullPointerException();
        while(iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

}

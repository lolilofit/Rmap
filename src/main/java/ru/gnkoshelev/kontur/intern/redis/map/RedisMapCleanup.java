package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class RedisMapCleanup implements Runnable {
    private String redisKey;
  //  private String changeCounterName;
    private JedisPool jedisPool;
    private AtomicBoolean isExecuted = new AtomicBoolean(false);
 //   private WeakReference<Set<String>> keySet = null;
 //   private WeakReference<LinkedHashSet<RedisEntry>> entrySet = null;

    public RedisMapCleanup(String redisKey, JedisPool jedisPool) {
        this.redisKey = redisKey;
     //   this.changeCounterName = changeCounterName;
        this.jedisPool = jedisPool;
    }


    @Override
    public void run() {
        if(isExecuted.compareAndSet(false, true)) {
            try (Jedis jedis = jedisPool.getResource()) {

                System.out.println(redisKey + " Cleanup");
               // Transaction transaction = jedis.multi();
                jedis.del(redisKey);
               // transaction.incr(changeCounterName);
              //  transaction.exec();
              //  if(keySet != null)
              //      Objects.requireNonNull(keySet.get()).clear();
              //  if(entrySet != null)
              //      Objects.requireNonNull(entrySet.get()).clear();;
            }
        }
    }
}

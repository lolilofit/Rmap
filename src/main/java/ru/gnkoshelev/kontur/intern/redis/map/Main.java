package ru.gnkoshelev.kontur.intern.redis.map;

import java.util.*;

public class Main {
    static void func() {
        /*
        RedisMap redisMap = new RedisMap();
        redisMap.put("1", "value1");
        redisMap.put("2", "value2");

        RedisMap redisMap1 = new RedisMap();
        redisMap1.put("1", "value1");
        redisMap1.put("2", "value2");

        System.out.println(redisMap.equals(redisMap1));
*/
        RedisMap redisMap = new RedisMap();
        System.out.println(redisMap.put("1", "value1"));
        System.out.println(redisMap.put("2", "value2"));
        System.out.println(redisMap.put("1", "chahgedValue"));
        System.out.println(redisMap.put("3", "value2"));

        Set<String> keySet = redisMap.keySet();
        keySet.remove("1");

        System.out.println(redisMap.put("4", "value2"));
        keySet.iterator();
    }

    public static void main(String[] args) {
      /*
      //iterator
      //values
      //what put returns
       */
/*

        Map<String, String> v = new HashMap<>();
        v.put("1", "val");
        v.put("1", "changed");
        v.put("3", "pro");

        Map<String, String> v1 = new HashMap<>();
        v1.put("1", "val");
        v1.put("1", "changed");
        v1.put("3", "pro");
        System.out.println(v.equals(v1));

        Set<String> keys = v.keySet();
        Iterator<String> it = keys.iterator();

        //v.put("2", "23");
        System.out.println("");

        //v.remove("2");
        //keys.remove("2");

        it.next();
        it.remove();

        System.out.println(it.next());

        keys.remove("1");
        System.out.println("");
*/

/*
        Map<String, String> v = new HashMap<>();
        v.put("1", "val");
        v.put("1", "changed");

        Collection<String> val = v.values();
        val.remove("changed");
        System.out.println("");

 */

        func();

/*
        for (int i = 1; i <= 10000; i++) {
            int[] a = new int[10000];
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }

*/
    }
}

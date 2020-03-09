package ru.gnkoshelev.kontur.intern.redis.map;

import java.util.*;

public class Main {
    public static void main(String[] args) {
      /*
      //iterator
      //values
      //what put returns
       */

        Map<String, String> v = new HashMap<>();
        v.put("1", "val");
        v.put("1", "changed");
        v.put("3", "pro");

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

/*
        Map<String, String> v = new HashMap<>();
        v.put("1", "val");
        v.put("1", "changed");

        Collection<String> val = v.values();
        val.remove("changed");
        System.out.println("");



       RedisMap redisMap = new RedisMap();
       redisMap.put("1", "value1");
       redisMap.put("2", "value2");

       Set<String> keySet = redisMap.keySet();
       keySet.remove("1");

        System.out.println("");

 */

    }
}

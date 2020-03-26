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

        RedisMap redisMap = new RedisMap();
        System.out.println(redisMap.put("1", "value1"));
        System.out.println(redisMap.put("2", "value2"));
        System.out.println(redisMap.put("1", "chahgedValue"));
        System.out.println(redisMap.put("3", "value2"));


         */
        /*
        Collection<String> values = redisMap.values();
        Iterator<String> vit = values.iterator();
        while(vit.hasNext()) {
            System.out.println(vit.next());
        }

        Set<Map.Entry<String, String>> entrySet = redisMap.entrySet();
        Iterator<Map.Entry<String, String>> ite = entrySet.iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> e = ite.next();
            System.out.println(e.getKey() + " " + e.getValue());
        }
        //Map.Entry<String, String> e = ite.next();
        //entrySet.remove(e);


        Map<String, String> example = new HashMap<>();
        example.put("1", "value1");
        example.put("2", "value2");
        example.put("1", "chahgedValue");
        example.put("3", "value2");
        System.out.println(redisMap.equals(example));

        Set<String> keySet = redisMap.keySet();
        keySet.remove("1");

        System.out.println(redisMap.put("4", "value2"));
        Iterator<String> iter = keySet.iterator();

        iter.next();
        iter.remove();

        while(iter.hasNext()) {
            System.out.println(iter.next());
        }

         */
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
        v.put("2", "valll");
        //v.put("4", "pro");

        Set<Map.Entry<String, String>> entrySet = v.entrySet();
        System.out.println(entrySet.contains("1"));

        Set<String> keys = v.keySet();
        String[] ar = new String[6];
        String[] a = keys.toArray(ar);
        a = keys.toArray(ar);
        System.out.println("");


 */
        /*
        Map<String, String> v1= new HashMap<>();
        v1.put("1", "changed");
        Map.Entry<String, String> entry = v1.entrySet().iterator().next();
        v.remove(entry);

        Collection<String> values = v.values();
        Set<String> cv = new LinkedHashSet<>();
        cv.add("pro");
        values.removeAll(cv);
        System.out.println("");

 */
        /*
        Set<Map.Entry<String, String>> entrySet = v.entrySet();

        Map<String, String> v1 = new HashMap<>();
        v1.put("3", "pro");
        Set<Map.Entry<String, String>> entrySet1 = v1.entrySet();
        Iterator<Map.Entry<String, String>> iterator1 = entrySet.iterator();
        Map.Entry<String, String> entry1 = iterator1.next();

        entrySet.remove(entry1);
        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            //entry.setValue("value entry changed");
        }

         */
        /*
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

 *

        Set<String> k = new LinkedHashSet<>();
        k.add("first");
        k.add("sec");
        k.add("third");

        k.toArray();


 */
       // Iterator<String> it = k.iterator();
       // it.next();
       // it.remove();
       // it.remove();


/*
        Map<String, Object> hm = new HashMap<>();
        Map<String, String> tm = new TreeMap<>();

        hm.put("val1", "a");
        tm.put("val1", "a");

        System.out.println(hm.equals(tm));

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

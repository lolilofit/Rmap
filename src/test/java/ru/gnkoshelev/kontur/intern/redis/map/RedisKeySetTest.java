package ru.gnkoshelev.kontur.intern.redis.map;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RedisKeySetTest {

    @Test
    public void keySetIterationTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");

        Set<String> keySet = map1.keySet();
        Iterator<String> setIterator = keySet.iterator();

        Assert.assertTrue(setIterator.hasNext());
        Assert.assertEquals(setIterator.next(), "one");

        Assert.assertTrue(setIterator.hasNext());
        Assert.assertEquals(setIterator.next(), "two");

        Assert.assertFalse(setIterator.hasNext());
    }

    @Test
    public void keySetUpdateTest() {
        Map<String, String> map = new RedisMap();
        Set<String> keySet = map.keySet();

        Assert.assertTrue(keySet.isEmpty());

        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keySet.size(), 0);

        map.put("one", "1");

        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(keySet.size(), 1);

        map.clear();

        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keySet.size(), 0);

        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        keySet.removeAll(list);

        Assert.assertFalse(map.containsKey("one"));
        Assert.assertFalse(map.containsKey("two"));

        keySet.clear();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keySet.size(), 0);
    }

    @Test
    public void toArraySetTest() {
        Map<String, String> map = new RedisMap();
        Set<String> keySet = map.keySet();

        Object[] keysArray = keySet.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);

        map.put("one", "1");
        map.put("two", "2");

        keysArray = keySet.toArray();

        Assert.assertEquals(map.size(), 2);

        Assert.assertEquals(keysArray.length, 2);
        Assert.assertEquals(keysArray[0], "one");
        Assert.assertEquals(keysArray[1], "two");

        map.clear();

        keysArray = keySet.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);
    }

    @Test
    public void toArrayAddSetTest() {
        Map<String, String> map = new RedisMap();
        Set<String> keySet = map.keySet();
        String[] array = new String[3];

        map.put("one", "1");
        map.put("two", "2");

        String[] keysArray = keySet.toArray(array);

        Assert.assertTrue(array == keysArray);
        Assert.assertEquals(map.size(),2);

        Assert.assertEquals(keysArray[0], "one");
        Assert.assertEquals(keysArray[1], "two");

        map.clear();

        keysArray = keySet.toArray(array);
        Assert.assertNotEquals(keysArray[0], "one");
        Assert.assertNotEquals(keysArray[1], "two");
    }

    @Test
    public void containsTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        Set<String> keySet = map.keySet();
        Assert.assertTrue(keySet.contains("one"));

        List<String> keys = new ArrayList<>();
        keys.add("one");
        keys.add("two");

        Assert.assertTrue(keySet.containsAll(keys));
    }

    @Test
    public void removeTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        map.put("four", "4");

        Set<String> keySet = map.keySet();
        keySet.remove("one");

        Assert.assertFalse(map.containsKey("one"));
        Assert.assertFalse(keySet.contains("one"));

        map.remove("two");

        Assert.assertFalse(map.containsKey("two"));
        Assert.assertFalse(keySet.contains("two"));

        Iterator<String> iterator = keySet.iterator();
        iterator.next();
        iterator.remove();

        iterator.next();

        map.put("f", "4");
        boolean wasCaught = false;
        try {
            iterator.hasNext();
        } catch (IllegalStateException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);

        wasCaught = false;
        try {
            iterator.next();
        } catch (IllegalStateException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);
    }

    @Test
    public void forEachRemainanigTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = map.keySet().iterator();
        iterator.forEachRemaining(s -> {stringBuilder.append(s);});
        Assert.assertEquals(stringBuilder.toString(), "onetwothree");
    }

    @Test
    public void removeWithIterator() {

        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        Iterator<String> iterator = map.keySet().iterator();
        iterator.next();
        iterator.remove();
        Assert.assertFalse(map.containsKey("one"));
        Assert.assertFalse(map.containsValue("1"));

        map.put("oneMore", "1");
        boolean wasCaught = false;
        try {
            iterator.hasNext();
        } catch (IllegalStateException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);

        wasCaught = false;
        try {
            iterator.next();
        } catch (IllegalStateException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);
    }

    @Test
    public void equalsTest() {
        Map<String, String> map1 = new RedisMap();
        Map<String, String> map2 = new RedisMap();
        Map<String, String> map3 = new RedisMap();
        Map<String, String> map4 = new RedisMap();

        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");

        map2.put("one", "1");
        map2.put("two", "2");
        map2.put("three", "1");

        map3.put("one", "1");
        map3.put("two", "2");
        map3.put("three", "1");

        map4.put("one", "1");

        Assert.assertEquals(map1.entrySet(), map2.entrySet());
        Assert.assertEquals(map2.entrySet(), map1.entrySet());

        Assert.assertEquals(map2.entrySet(), map3.entrySet());
        Assert.assertEquals(map1.entrySet(), map3.entrySet());
        Assert.assertEquals(map3.entrySet(), map1.entrySet());

        Assert.assertEquals(map1.entrySet(), map1.entrySet());

        Assert.assertNotEquals(map1.entrySet(), map4.entrySet());
        Assert.assertNotEquals(map4.entrySet(), map1.entrySet());

        Assert.assertNotEquals(map1.entrySet(), null);
        Assert.assertNotEquals(map1.entrySet(), "someOtherType");
    }

    @Test
    public void hashCodeTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");

        Map<String, String> map2 = new RedisMap();
        map2.put("one", "1");
        map2.put("two", "2");
        map2.put("three", "1");

        Map<String, String> map3 = new RedisMap();
        map3.put("one", "1");

        int hashCode1 = map1.entrySet().hashCode();
        int hashCode2 = map1.entrySet().hashCode();
        Assert.assertEquals(hashCode1, hashCode2);

        Assert.assertEquals(map1.entrySet().hashCode(), map2.entrySet().hashCode());

        Assert.assertNotEquals(map1.entrySet().hashCode(), map3.entrySet().hashCode());
    }
}

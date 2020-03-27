package ru.gnkoshelev.kontur.intern.redis.map;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RedisCollectionTest {
    @Test
    public void valueSetIterationTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");

        Collection<String> values = map1.values();
        Iterator<String> setIterator = values.iterator();

        Assert.assertTrue(setIterator.hasNext());
        Assert.assertEquals(setIterator.next(), "1");
        Assert.assertTrue(setIterator.hasNext());
        Assert.assertEquals(setIterator.next(), "2");
        Assert.assertFalse(setIterator.hasNext());

        values.clear();
        Assert.assertEquals(map1.size(), 0);
        Assert.assertEquals(values.size(), 0);
    }

    @Test
    public void entrySetIterationTest() {
        Map<String, String> map1 = new RedisMap();

        Collection<String> values = map1.values();

        Assert.assertTrue(values.isEmpty());

        map1.put("one", "1");
        map1.put("two", "2");

        Iterator<String> setIterator = values.iterator();
        String entry;

        Assert.assertTrue(setIterator.hasNext());
        entry = setIterator.next();
        Assert.assertEquals(entry, "1");

        Assert.assertTrue(setIterator.hasNext());
        entry = setIterator.next();
        Assert.assertEquals(entry, "2");

        Assert.assertFalse(setIterator.hasNext());
    }

    @Test
    public void toArraySetTest() {
        Map<String, String> map = new RedisMap();
        Collection<String> values = map.values();

        Object[] keysArray = values.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);

        map.put("one", "1");
        map.put("two", "2");

        keysArray = values.toArray();

        Assert.assertEquals(map.size(), 2);

        Assert.assertEquals(keysArray.length, 2);
        Assert.assertEquals(keysArray[0], "1");
        Assert.assertEquals(keysArray[1], "2");

        map.clear();

        keysArray = values.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);
    }

    @Test
    public void toArrayAddSetTest() {
        Map<String, String> map = new RedisMap();
        Collection<String> values = map.values();
        String[] array = new String[3];

        map.put("one", "1");
        map.put("two", "2");

        String[] keysArray = values.toArray(array);

        Assert.assertTrue(array == keysArray);
        Assert.assertEquals(map.size(),2);

        Assert.assertEquals(keysArray[0], "1");
        Assert.assertEquals(keysArray[1], "2");

        map.clear();

        keysArray = values.toArray(array);
        Assert.assertNotEquals(keysArray[0], "1");
        Assert.assertNotEquals(keysArray[1], "2");
    }


    @Test
    public void containsTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        Collection<String> values = map.values();
        Assert.assertTrue(values.contains("1"));

        List<String> keys = new ArrayList<>();
        keys.add("2");
        keys.add("3");

        Assert.assertTrue(values.containsAll(keys));
    }

    @Test
    public void removeTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "2");
        map.put("four", "2");
        map.put("threeDouble", "3");
        map.put("five", "5");
        map.put("six", "6");

        Collection<String> values = map.values();
        values.remove("1");

        Assert.assertFalse(map.containsKey("one"));
        Assert.assertFalse(map.containsValue("1"));
        Assert.assertFalse(values.contains("1"));

        map.remove("two");

        Assert.assertFalse(map.containsKey("two"));
        Assert.assertTrue(values.contains("2"));

        List<String> elementsToRemove = new ArrayList<>();
        elementsToRemove.add("2");
        elementsToRemove.add("3");

        values.removeAll(elementsToRemove);

        Assert.assertFalse(values.contains("2"));
        Assert.assertFalse(values.contains("3"));
        Assert.assertFalse(map.containsKey("three"));
        Assert.assertFalse(map.containsKey("threeDouble"));
        Assert.assertFalse(map.containsKey("four"));

        Iterator<String> iterator = values.iterator();
        iterator.next();
        iterator.remove();

        Assert.assertFalse(map.containsKey("five"));

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
        Iterator<String> iterator = map.values().iterator();
        iterator.forEachRemaining(s -> {stringBuilder.append(s);});
        Assert.assertEquals(stringBuilder.toString(), "123");
    }


    @Test
    public void removeWithIterator() {

        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        Iterator<String> iterator = map.values().iterator();
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

        Assert.assertEquals(map1.values(), map2.values());
        Assert.assertEquals(map2.values(), map1.values());

        Assert.assertEquals(map2.values(), map3.values());
        Assert.assertEquals(map1.values(), map3.values());
        Assert.assertEquals(map3.values(), map1.values());

        Assert.assertEquals(map1.values(), map1.values());

        Assert.assertNotEquals(map1.values(), map4.values());
        Assert.assertNotEquals(map4.values(), map1.values());

        Assert.assertNotEquals(map1.values(), null);
        Assert.assertNotEquals(map1.values(), 1);
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

        int hashCode1 = map1.values().hashCode();
        int hashCode2 = map1.values().hashCode();
        Assert.assertEquals(hashCode1, hashCode2);

        Assert.assertEquals(map1.values().hashCode(), map2.values().hashCode());

        Assert.assertNotEquals(map1.values().hashCode(), map3.values().hashCode());
    }

    @Test
    public void retainAllTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");
        Collection<String> values = map1.values();

        List<String> list = new ArrayList<>();
        list.add("1");

        values.retainAll(list);

        Assert.assertTrue(map1.containsKey("one"));
        Assert.assertFalse(map1.containsKey("two"));
        Assert.assertTrue(map1.containsKey("three"));
    }
}

package ru.gnkoshelev.kontur.intern.redis.map;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RedisEntrySetTest {
    @Test
    public void entrySetIterationTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");

        Set<Map.Entry<String, String>> entrySet = map1.entrySet();
        Iterator<Map.Entry<String, String>> setIterator = entrySet.iterator();
        Map.Entry<String, String> entry;

        Assert.assertTrue(setIterator.hasNext());
        entry = setIterator.next();
        Assert.assertEquals(entry.getKey(), "one");
        Assert.assertEquals(entry.getValue(), "1");

        Assert.assertTrue(setIterator.hasNext());
        entry = setIterator.next();
        Assert.assertEquals(entry.getKey(), "two");
        Assert.assertEquals(entry.getValue(), "2");

        Assert.assertFalse(setIterator.hasNext());
    }

    @Test
    public void entryTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");

        Map<String, String> map2 = new RedisMap();
        map2.put("one", "1");

        Iterator<Map.Entry<String, String>>  iterator = map.entrySet().iterator();
        Assert.assertTrue(iterator.hasNext());

        Map.Entry<String, String> entry = iterator.next();

        Map.Entry<String, String>  iterator1 = map.entrySet().iterator().next();
        Assert.assertEquals(entry, iterator1);

        Map.Entry<String, String>  iterator2 = map.entrySet().iterator().next();
        Assert.assertEquals(entry, iterator2);

        Assert.assertEquals(entry.getKey(), "one");
        Assert.assertEquals(entry.getValue(), "1");

        Assert.assertEquals(entry.setValue("newValue"), "1");
        Assert.assertEquals(map.get("one"), "newValue");
    }

    @Test
    public void entrySetUpdateTest() {
        Map<String, String> map = new RedisMap();
        Set<Map.Entry<String, String>> entrySet = map.entrySet();

        Assert.assertTrue(entrySet.isEmpty());

        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(entrySet.size(), 0);

        map.put("one", "1");

        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(entrySet.size(), 1);

        map.clear();

        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(entrySet.size(), 0);

        map.put("one", "1");
        map.put("two", "2");

        Assert.assertTrue(map.containsKey("one"));
        Assert.assertTrue(map.containsKey("two"));

        entrySet.clear();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(entrySet.size(), 0);
    }

    @Test
    public void toArraySetTest() {
        Map<String, String> map = new RedisMap();
        Set<Map.Entry<String, String>> entrySet = map.entrySet();

        Object[] keysArray = entrySet.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);

        map.put("one", "1");
        map.put("two", "2");

        keysArray = entrySet.toArray();

        Assert.assertEquals(map.size(), 2);

        Assert.assertEquals(keysArray.length, 2);
        Assert.assertEquals(((Map.Entry<?, ?>)keysArray[0]).getKey(), "one");
        Assert.assertEquals(((Map.Entry<?, ?>)keysArray[0]).getValue(), "1");

        Assert.assertEquals(((Map.Entry<?, ?>)keysArray[1]).getKey(), "two");
        Assert.assertEquals(((Map.Entry<?, ?>)keysArray[1]).getValue(), "2");

        map.clear();

        keysArray = entrySet.toArray();
        Assert.assertEquals(map.size(), 0);
        Assert.assertEquals(keysArray.length, 0);
    }

    @Test
    public void toArrayAddSetTest() {
        Map<String, String> map = new RedisMap();
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        Map.Entry[] array = new Map.Entry[3];

        map.put("one", "1");
        map.put("two", "2");

        Map.Entry[] keysArray = entrySet.toArray(array);

        Assert.assertTrue(array == keysArray);
        Assert.assertEquals(map.size(),2);

        Assert.assertEquals(keysArray[0].getKey(), "one");
        Assert.assertEquals(keysArray[0].getValue(), "1");
        Assert.assertEquals(keysArray[1].getKey(), "two");
        Assert.assertEquals(keysArray[1].getValue(), "2");

        map.clear();

        keysArray = entrySet.toArray(array);
        Assert.assertNull(keysArray[0]);
        Assert.assertNull(keysArray[1]);
    }

    @Test
    public void containsTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");

        Map<String, String> map1 = new HashMap<>();
        map1.put("one", "1");
        Map.Entry<String, String> entry1 = map1.entrySet().iterator().next();

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        Assert.assertTrue(entrySet.contains(entry1));

        List<Map.Entry<String, String>> keys = new ArrayList<>();
        keys.add(entry1);

        Assert.assertTrue(entrySet.containsAll(keys));
    }

    @Test
    public void removeTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        map.put("four", "4");

        Map<String, String> map1 = new HashMap<>();
        map1.put("one", "1");
        map1.put("two", "2");
        Iterator<Map.Entry<String, String>> iterator1 = map1.entrySet().iterator();
        Map.Entry<String, String> entry1 = iterator1.next();
        Map.Entry<String, String> entry2 = iterator1.next();

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        entrySet.remove(entry1);

        Assert.assertFalse(map.containsKey("one"));
        Assert.assertFalse(entrySet.contains(entry1));

        map.remove("two");

        Assert.assertFalse(map.containsKey("two"));
        Assert.assertFalse(entrySet.contains(entry2));

        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
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
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        iterator.forEachRemaining(s -> {stringBuilder.append(s.getKey() + s.getValue());});
        Assert.assertEquals(stringBuilder.toString(), "one1two2three3");
    }

    @Test
    public void removeWithIterator() {

        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
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

    @Test
    public void removeAllTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");

        Map<String, String> map3 = new RedisMap();
        map3.put("one", "1");

        Set<Map.Entry<String, String>> entrySet = map1.entrySet();

        List<Map.Entry<String, String>> list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = map3.entrySet().iterator();
        list.add(iterator.next());

        entrySet.removeAll(list);

        Assert.assertFalse(map1.containsKey("one"));
        Assert.assertTrue(map1.containsKey("two"));
        Assert.assertTrue(map1.containsKey("three"));
    }


    @Test
    public void retainAllTest() {
        Map<String, String> map1 = new RedisMap();
        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");

        Map<String, String> map3 = new RedisMap();
        map3.put("one", "1");

        Set<Map.Entry<String, String>> entrySet = map1.entrySet();

        List<Map.Entry<String, String>> list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = map3.entrySet().iterator();
        list.add(iterator.next());

        entrySet.retainAll(list);

        Assert.assertTrue(map1.containsKey("one"));
        Assert.assertFalse(map1.containsKey("two"));
        Assert.assertFalse(map1.containsKey("three"));
    }
}

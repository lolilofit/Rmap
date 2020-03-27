package ru.gnkoshelev.kontur.intern.redis.map;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Gregory Koshelev
 */
public class RedisMapTest {
    @Test
    public void baseTests() {
        Map<String, String> map1 = new RedisMap();
        Map<String, String> map2 = new RedisMap();

        map1.put("one", "1");

        map2.put("one", "ONE");
        map2.put("two", "TWO");

        Assert.assertEquals("1", map1.get("one"));
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals(2, map2.size());

        map1.put("one", "first");

        Assert.assertEquals("first", map1.get("one"));
        Assert.assertEquals(1, map1.size());

        Assert.assertTrue(map1.containsKey("one"));
        Assert.assertFalse(map1.containsKey("two"));

        Set<String> keys2 = map2.keySet();
        Assert.assertEquals(2, keys2.size());
        Assert.assertTrue(keys2.contains("one"));
        Assert.assertTrue(keys2.contains("two"));

        Collection<String> values1 = map1.values();
        Assert.assertEquals(1, values1.size());
        Assert.assertTrue(values1.contains("first"));
    }

    @Test
    public void severalUsersTest() {
        Map<String, String> map1 = new RedisMap();

        map1.put("one", "1");

        Map<String, String> map2 = new RedisMap(map1.toString());

        Assert.assertEquals(map1.size(), map2.size());
        Assert.assertTrue(map2.containsKey("one"));
        Assert.assertTrue(map2.containsValue("1"));

        map2.put("two", "2");

        Assert.assertTrue(map2.containsKey("two"));
        Assert.assertTrue(map2.containsValue("2"));

        Assert.assertTrue(map1.containsKey("two"));
        Assert.assertTrue(map1.containsValue("2"));

        map2.remove("two");

        Assert.assertFalse(map2.containsKey("two"));
        Assert.assertFalse(map2.containsValue("2"));

        Assert.assertFalse(map1.containsKey("two"));
        Assert.assertFalse(map1.containsValue("2"));
    }

    @Test
    public void emptyMapTest() {
        Map<String, String> map = new RedisMap();

        Assert.assertTrue(map.isEmpty());

        Assert.assertNull(map.put("two", "2"));
        Assert.assertEquals(map.put("two", "changed"), "2");

        map.put("one", "1");
        map.put("three", "3");

        Assert.assertFalse(map.isEmpty());
        map.clear();
        Assert.assertTrue(map.isEmpty());
    }


    @Test
    public void removeMapTest() {
        Map<String, String> map = new RedisMap();
        map.put("one", "1");
        map.put("two", "2");

        Assert.assertTrue(map.containsKey("one"));
        Assert.assertTrue(map.containsKey("two"));
        Assert.assertEquals(map.remove("one"), "1");
        Assert.assertFalse(map.containsKey("one"));
        Assert.assertTrue(map.containsKey("two"));

        Assert.assertNull(map.remove(1));
    }

    @Test
    public void putMapTest() {
        Map<String, String> map = new RedisMap();
        boolean wasCaught = false;
        try {
            map.put("1", null);
        }
        catch (NullPointerException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);

        wasCaught = false;
        try {
            map.put(null, "one");
        }
        catch (NullPointerException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);

        wasCaught = false;
        try {
            map.putAll(null);
        }
        catch (NullPointerException e) {
            wasCaught = true;
        }
        Assert.assertTrue(wasCaught);

        map.put("1", "1");
        //wrong type
        Assert.assertFalse(map.containsKey(1));
        Assert.assertFalse(map.containsValue(1));
        Assert.assertNull(map.get("noSuchElement"));

        Map<String, String> mapToAdd = new HashMap<>();
        mapToAdd.put("added1", "a1");
        mapToAdd.put("added2", "a2");

        map.putAll(mapToAdd);
        Assert.assertTrue(map.containsKey("added1"));
        Assert.assertTrue(map.containsKey("added2"));
        Assert.assertTrue(map.containsValue("a1"));
        Assert.assertTrue(map.containsValue("a2"));

        Map<String, String> redisMapToAdd = new RedisMap();
        redisMapToAdd.put("redisAdd1", "r1");
        redisMapToAdd.put("redisAdd2", "r2");

        map.putAll(redisMapToAdd);
        Assert.assertTrue(map.containsKey("redisAdd1"));
        Assert.assertTrue(map.containsKey("redisAdd2"));
        Assert.assertTrue(map.containsValue("r1"));
        Assert.assertTrue(map.containsValue("r2"));
    }

    @Test
    public void equalsTest() {
        Map<String, String> map1 = new RedisMap();
        Map<String, String> map2 = new RedisMap();
        Map<String, String> map4 = new RedisMap();
        Map<String, String> map5 = new RedisMap();

        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "1");

        map2.put("one", "1");
        map2.put("two", "2");
        map2.put("three", "1");

        map4.put("one", "1");

        map5.put("one", "1");
        map5.put("two", "2");
        map5.put("three", "1");

        Assert.assertEquals(map1, map2);
        Assert.assertEquals(map2, map1);

        Assert.assertEquals(map2, map5);
        Assert.assertEquals(map1, map5);
        Assert.assertEquals(map5, map1);

        Assert.assertEquals(map1, map1);

        Assert.assertNotEquals(map1, map4);
        Assert.assertNotEquals(map4, map1);

        Assert.assertNotEquals(map1, null);
        Assert.assertNotEquals(map1, "someOtherType");
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

        int hashCode1 = map1.hashCode();
        int hashCode2 = map1.hashCode();
        Assert.assertEquals(hashCode1, hashCode2);

        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        Assert.assertNotEquals(map1.hashCode(), map3.hashCode());
    }
}

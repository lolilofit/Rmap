package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class ScriptsStorage {
    private static final Map<String, String> scripts;
    private static List<String> keyParam;

    static {
        Map<String, String> modScriptMap = new HashMap<>();

        try {
            String checkUpdateWithRemove = Files.readString(Path.of("src/main/Lua/CheckUpdateWithRemove.lua"));
            modScriptMap.put("checkUpdateWithRemove", checkUpdateWithRemove);

            String containsValue = Files.readString(Path.of("src/main/Lua/ContainsValue.lua"));
            modScriptMap.put("containsValue", containsValue);

            String removeByValue = Files.readString(Path.of("src/main/Lua/RemoveByValue.lua"));
            modScriptMap.put("removeByValue", removeByValue);

            String removeByCollection = Files.readString(Path.of("src/main/Lua/RemoveByCollection.lua"));
            modScriptMap.put("removeByCollection", removeByCollection);

            String removeNotInCollection = Files.readString(Path.of("src/main/Lua/RetainAllValue.lua"));
            modScriptMap.put("retainAllValue", removeNotInCollection);

            String testAnsIncr = Files.readString(Path.of("src/main/Lua/TestAndIncr.lua"));
            modScriptMap.put("testAndIncr", testAnsIncr);

            String retainAllKey = Files.readString(Path.of("src/main/Lua/RetainAllKey.lua"));
            modScriptMap.put("retainAllKey", retainAllKey);

            String cleanRedisMap = Files.readString(Path.of("src/main/Lua/CleanRedisMap.lua"));
            modScriptMap.put("cleanRedisMap", cleanRedisMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        scripts = Collections.unmodifiableMap(modScriptMap);
        keyParam = new ArrayList<>(1);
        keyParam.add("0");
    }

    public static Long checkUpdateWithRemove(Jedis jedis, List<String> params) {
        Object result = jedis.eval(scripts.get("checkUpdateWithRemove"), keyParam, params);
        return (Long)result;
    }

    public static String getContainsValueScript() {
        return scripts.get("containsValue");
    }

    public static String getRemoveByValueScript() {
        return scripts.get("removeByValue");
    }

    public static String getRemoveByCollectionScript() {
        return scripts.get("removeByCollection");
    }

    public static String getTestAndIncrScript() {
        return scripts.get("testAndIncr");}

    public static String getRetainAllValueScript() {
        return scripts.get("retainAllValue"); }

    public static String getRetainAllKeyScript() {
        return scripts.get("retainAllKey");
    }

    public static String getCleanMapScript() {
        return scripts.get("cleanRedisMap");
    }
}

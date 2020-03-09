package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;

import java.util.*;

public class UpdatedChecker {
    private static final Map<String, String> scripts;

    static {
        Map<String, String> modScriptMap = new HashMap<>();
        StringBuilder checkUpdate = new StringBuilder();
        checkUpdate.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  ")
                .append("return {change_counter, nil}");
        modScriptMap.put("checkUpdates", checkUpdate.toString());

        StringBuilder checkUpdateWithRemove = new StringBuilder();
        checkUpdateWithRemove.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  ")
                .append("return {change_counter, nil}");
        modScriptMap.put("checkUpdateWithRemove", checkUpdateWithRemove.toString());

        scripts = Collections.unmodifiableMap(modScriptMap);
    }

    static Long checkForUpdatedWithScript(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params, String scriptName) {
        Object values = jedis.eval(scripts.get(scriptName), keysParam, params);
        Collection castedValues = (Collection) values;
        Iterator iterator = castedValues.iterator();
        Long changeCounter = (Long) iterator.next();
        if(castedValues.size() > 1) {
            Set<String> currentKeySet = (Set<String>) iterator.next();
            savedKeySet.clear();
            savedKeySet.addAll(currentKeySet);
        }
        return changeCounter;
    }

    static Long checkForUpdates(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params) {
        return checkForUpdatedWithScript(jedis, savedKeySet, keysParam, params, "checkUpdates");
    }

    static Long checkUpdatesWithRemove(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params) {
        return checkForUpdatedWithScript(jedis, savedKeySet, keysParam, params, "checkUpdateWithRemove");
    }
}

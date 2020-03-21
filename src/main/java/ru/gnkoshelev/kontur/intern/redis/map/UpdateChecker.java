package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;

import java.util.*;

public class UpdateChecker {
    private static final Map<String, String> scripts;

    static {
        Map<String, String> modScriptMap = new HashMap<>();
        StringBuilder checkUpdate = new StringBuilder();
        checkUpdate.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                //.append("redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  ")
                .append("return {change_counter}");
        modScriptMap.put("checkUpdates", checkUpdate.toString());

        StringBuilder checkUpdateWithRemove = new StringBuilder();
        checkUpdateWithRemove.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("local deleted_num = redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, deleted_num, redis.call(\"hkeys\", ARGV[3])} end  ")
                .append("return {change_counter, deleted_num}");
        modScriptMap.put("checkUpdateWithRemove", checkUpdateWithRemove.toString());

        StringBuilder checkUpdateWithRemoveAll = new StringBuilder();
        checkUpdateWithRemoveAll.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) local deleted_num = 0")
                .append("for i = 4, #ARGV, 1 do deleted_num = deleted_num + redis.call(\"hdel\", ARGV[3], ARGV[i]) end")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hkeys\", ARGV[3])} end  ")
                .append("return {change_counter}");
        modScriptMap.put("checkUpdateWithRemoveAll", checkUpdateWithRemoveAll.toString());

        scripts = Collections.unmodifiableMap(modScriptMap);
    }

    static List<Long> checkForUpdatedWithScript(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params, String scriptName, int minResultsNumber) {
        List<Long> result = new ArrayList<>(2);
        Object values = jedis.eval(scripts.get(scriptName), keysParam, params);
        Collection<Object> castedValues = (Collection<Object>) values;
        Iterator<Object> iterator = castedValues.iterator();
        //casting
        for(int i = 0; i < minResultsNumber; i++) {
            Object resultElement = iterator.next();
            if(resultElement instanceof Long)
                result.add((Long)resultElement);
        }

        if(castedValues.size() > minResultsNumber) {
            if(savedKeySet != null) {
                Object lastResult = iterator.next();
                Set<String> currentKeySet = (Set<String>) lastResult;
                savedKeySet.clear();
                savedKeySet.addAll(currentKeySet);
            }
        }
        return result;
    }

    static Long checkForUpdates(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params) {
        List<Long> result = checkForUpdatedWithScript(jedis, savedKeySet, keysParam, params, "checkUpdates", 1);
        return result.get(0);
    }

    static List<Long> checkUpdatesWithRemove(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params) {
        return checkForUpdatedWithScript(jedis, savedKeySet, keysParam, params, "checkUpdateWithRemove", 2);
    }

    static List<Long> checkUpdateWithRemoveAll(Jedis jedis, Set<String> savedKeySet, List<String> keysParam, List<String> params) {
        return checkForUpdatedWithScript(jedis, savedKeySet, keysParam, params, "checkUpdateWithRemoveAll", 2);
    }
}

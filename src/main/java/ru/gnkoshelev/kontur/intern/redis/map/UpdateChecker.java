package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.*;

public class UpdateChecker {
    private final Map<String, String> scripts;
    private JedisPool jedisPool;
    private MapParams mapParams;
    private List<String> keysParam;
    private LinkedHashSet<String> keySetForAdding = null;
    private LinkedHashSet<RedisEntry> redisEntryForAdding = null;


    public UpdateChecker(JedisPool jedisPool, MapParams mapParams, List<String> keysParam) {
        this.jedisPool = jedisPool;
        this.mapParams = mapParams;
        this.keysParam = keysParam;
    }

    public void setKeySetForAdding(LinkedHashSet<String> keySetForAdding) {
        this.keySetForAdding = keySetForAdding;
    }

    public void setRedisEntryForAdding(LinkedHashSet<RedisEntry> redisEntryForAdding) {
        this.redisEntryForAdding = redisEntryForAdding;
    }

    {
        Map<String, String> modScriptMap = new HashMap<>();

        StringBuilder checkUpdate = new StringBuilder();
        checkUpdate.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hgetall\", ARGV[3])} end  ")
                .append("return {change_counter}");
        modScriptMap.put("checkUpdates", checkUpdate.toString());

        StringBuilder checkUpdateWithRemove = new StringBuilder();
        checkUpdateWithRemove.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("local deleted_num = redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, deleted_num, redis.call(\"hgetall\", ARGV[3])} end  ")
                .append("return {change_counter, deleted_num}");
        modScriptMap.put("checkUpdateWithRemove", checkUpdateWithRemove.toString());

        StringBuilder checkUpdateWithRemoveAll = new StringBuilder();
        checkUpdateWithRemoveAll.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) local deleted_num = 0")
                .append("for i = 4, #ARGV, 1 do deleted_num = deleted_num + redis.call(\"hdel\", ARGV[3], ARGV[i]) end")
                .append("if(change_counter ~= current_number) then ")
                .append("return {change_counter, redis.call(\"hgetall\", ARGV[3])} end  ")
                .append("return {change_counter}");
        modScriptMap.put("checkUpdateWithRemoveAll", checkUpdateWithRemoveAll.toString());

        scripts = Collections.unmodifiableMap(modScriptMap);
    }

     private List<Long> checkForUpdatedWithScript(Jedis jedis, List<String> params, String scriptName, int minResultsNumber, Set<String> objectsToRemove) {
         List<Long> result = new ArrayList<>(2);
         Object values = jedis.eval(scripts.get(scriptName), keysParam, params);
         Collection<Object> castedValues = (Collection<Object>) values;
         Iterator<Object> iterator = castedValues.iterator();

         for (int i = 0; i < minResultsNumber; i++) {
             Object resultElement = iterator.next();
             if (resultElement instanceof Long)
                 result.add((Long) resultElement);
         }

         if (iterator.hasNext()) {
             ArrayList<String> currentKeySet = (ArrayList<String>) iterator.next();
             Objects.requireNonNull(keySetForAdding).clear();
             Objects.requireNonNull(redisEntryForAdding).clear();

             Set<RedisEntry> entrySet = new LinkedHashSet<>();
             Set<String> keys = new LinkedHashSet<>();
             for (int i = 0; i < currentKeySet.size() - 1; i = i + 2) {
                 entrySet.add(new RedisEntry(currentKeySet.get(i), currentKeySet.get(i + 1), jedisPool, mapParams));
                keys.add(currentKeySet.get(i));
             }
             Objects.requireNonNull(redisEntryForAdding).addAll(entrySet);
             Objects.requireNonNull(keySetForAdding).addAll(keys);

         }
        return result;
    }

    public Long checkForUpdates(Jedis jedis, List<String> params) {
        List<Long> result = checkForUpdatedWithScript(jedis, params, "checkUpdates", 1, null);
        return result.get(0);
    }

    public List<Long> checkUpdatesWithRemove(Jedis jedis, List<String> params, String keyToRemove) {
        Set<String> objectsToRemove = new LinkedHashSet<>();
        objectsToRemove.add(keyToRemove);
        return checkForUpdatedWithScript(jedis, params, "checkUpdateWithRemove", 2, objectsToRemove);
    }

    public List<Long> checkUpdateWithRemoveAll(Jedis jedis, List<String> params, Set<String> objectsToRemove) {
        return checkForUpdatedWithScript(jedis, params, "checkUpdateWithRemoveAll", 2, objectsToRemove);
    }
}

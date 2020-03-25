package ru.gnkoshelev.kontur.intern.redis.map;

import redis.clients.jedis.Jedis;
import java.util.*;


public class ScriptsStorage {
    private static final Map<String, String> scripts;
    private static List<String> keyParam;

    static {
        Map<String, String> modScriptMap = new HashMap<>();

        StringBuilder checkUpdateWithRemove = new StringBuilder();
        checkUpdateWithRemove.append("local change_counter = tonumber(ARGV[1]) local a = redis.call(\"GET\", ARGV[2]) local current_number = tonumber(a) ")
                .append("if(change_counter ~= current_number) then ")
                .append("return current_number end ")
                .append("local deleted_num = redis.call(\"hdel\", ARGV[3], ARGV[4]) ")
                .append("redis.call(\"incr\", ARGV[2]) ")
                .append("return current_number");
        modScriptMap.put("checkUpdateWithRemove", checkUpdateWithRemove.toString());

        String containsValue = "local val = ARGV[1] local values = redis.call(\"HVALS\", ARGV[2]) for i, name in ipairs(values) do if name == val then return 1 end end return 0";
        modScriptMap.put("containsValue", containsValue);

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
}

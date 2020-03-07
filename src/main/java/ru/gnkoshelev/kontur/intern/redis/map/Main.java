package ru.gnkoshelev.kontur.intern.redis.map;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, String> v = new HashMap<>();
        v.put("1", "val");
        v.put("1", "changed");
        System.out.println("");

    }
}

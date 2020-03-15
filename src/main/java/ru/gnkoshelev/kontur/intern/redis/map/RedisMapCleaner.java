package ru.gnkoshelev.kontur.intern.redis.map;

import java.lang.ref.Cleaner;

public class RedisMapCleaner {
    private static Cleaner cleaner = Cleaner.create();

    public static void register(Object registeringObject, Runnable cleanerFunction) {
        cleaner.register(registeringObject, cleanerFunction);
    }

}

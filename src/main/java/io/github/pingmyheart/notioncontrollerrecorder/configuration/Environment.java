package io.github.pingmyheart.notioncontrollerrecorder.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Environment {
    private static final Map<String, String> env = new HashMap<>();

    public static void setEnv(String key, String value) {
        env.put(key, value);
    }

    public static String get(String key) {
        return env.get(key);
    }
}

package org.ww.ai.tools;

import java.util.HashMap;
import java.util.Map;

public enum GlobalFakeRegistry {
    GLOBAL_FAKE_REGISTRY;

    private static final Map<String, Object> objectMap = new HashMap<>();

    public void add(Object o) {
        objectMap.put(o.getClass().getCanonicalName(), o);
    }

    public Object get(String clazzName) {
        return objectMap.get(clazzName);
    }

    public boolean hasClazzName(String clazzName) {
        return objectMap.containsKey(clazzName);
    }

    public int size() {
        return objectMap.size();
    }
}

package org.ww.ai.tools;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum TimeMeasurement {
    TIME_MEASUREMENT;

    private final Map<String, TimeNameHolder> timers = new HashMap<>();

    public String start( String name ) {
        UUID uuid = UUID.randomUUID();
        TimeNameHolder holder = new TimeNameHolder();
        holder.name = name;
        holder.timeNanos = System.nanoTime();
        timers.put(uuid.toString(), holder);
        return uuid.toString();
    }

    public void stop( String uuid ) {
        if ( ! timers.containsKey(uuid) ) {
            Log.e("TIMER", "Unknown timer uuid: " + uuid);
        }
        TimeNameHolder holder = timers.get(uuid);
        long elapsed = (System.nanoTime() - holder.timeNanos) / 1000000;
        Log.d("TIMER", holder.name + " took " + elapsed + " ms");
        timers.remove(uuid);
    }

    private static class TimeNameHolder {
        String name;
        Long timeNanos;
    }
}
package org.ww.ai.event;

import android.util.Log;

import org.ww.ai.enumif.EventTypes;
import org.ww.ai.enumif.ReceiveEventIF;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum EventBroker {
    EVENT_BROKER;

    private final Map<ReceiveEventIF, List<EventTypes>> receivers = new HashMap<>();

    public void registerReceiver(ReceiveEventIF receiveEvent, EventTypes... eventTypes) {
        if (eventTypes.length == 0) {
            receivers.put(receiveEvent, Collections.emptyList());
        } else {
            receivers.put(receiveEvent, List.of(eventTypes));
        }
    }

    public void notifyReceivers(EventTypes eventType, Object... objects) {
        receivers.keySet().forEach(k -> {
            if (Objects.requireNonNull(receivers.get(k)).stream().anyMatch(f -> eventType == f)) {
                Log.d("BROKER", "sending " + eventType + " to " +
                        k + ", eventObjects: " + Arrays.toString(objects));
                k.receiveEvent(objects.length > 0 ? objects : null);
            }
        });
    }

}

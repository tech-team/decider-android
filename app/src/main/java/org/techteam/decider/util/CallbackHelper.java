package org.techteam.decider.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CallbackHelper<K, C> {
    public static enum AddStatus {
        NEW_CB,
        HAD_CB
    }

    private Map<K, List<C>> callbacks = new HashMap<K, List<C>>();

    public CallbackHelper() {
    }

    public AddStatus addCallback(K key, C callback) {
        AddStatus status;
        List<C> list = callbacks.get(key);
        if (list == null) {
            list = new LinkedList<C>();
            callbacks.put(key, list);
            status = AddStatus.NEW_CB;
        } else {
            status = AddStatus.HAD_CB;
        }
        list.add(callback);
        return status;
    }

    public List<C> getCallbacks(K key) {
        return callbacks.get(key);
    }

    public void removeCallbacks(K key) {
        callbacks.remove(key);
    }
}

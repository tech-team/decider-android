package org.techteam.decider.net2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UrlParams implements Iterable<UrlParams.UrlParam<?>> {
    private List<UrlParam<?>> params = new LinkedList<>();

    public static class UrlParam<T> {
        private String key;
        private T value;

        public UrlParam(String key, T value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return getKey() + ": " + getValue().toString();
        }
    }

    public UrlParams() {
    }

    public List<UrlParam<?>> getParams() {
        return params;
    }

    @Override
    public Iterator<UrlParam<?>> iterator() {
        return params.iterator();
    }

    public UrlParams add(String key, String value) {
        params.add(new UrlParam<>(key, value));
        return this;
    }

    public UrlParams add(String key, int value) {
        params.add(new UrlParam<>(key, Integer.toString(value)));
        return this;
    }

    public UrlParams add(String key, double value) {
        params.add(new UrlParam<>(key, Double.toString(value)));
        return this;
    }

    public UrlParams add(String key, Object value) {
        params.add(new UrlParam<>(key, value));
        return this;
    }

    public UrlParams add(UrlParam p) {
        params.add(p);
        return this;
    }

    @SuppressWarnings("unchecked")
    public UrlParams replace(String key, Object newValue) {
        for (UrlParam p : params) {
            if (p.getKey().equals(key)) {
                p.setValue(newValue);
                break;
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Object get(String key) {
        for (UrlParam p : params) {
            if (p.getKey().equals(key)) {
                return p.getValue();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

}

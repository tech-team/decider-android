package org.techteam.decider.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UrlParams implements Iterable<UrlParams.UrlParam> {
    private List<UrlParam> params = new LinkedList<UrlParam>();

    public static class UrlParam {
        private String key;
        private String value;

        public UrlParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public UrlParams() {
    }

    @Override
    public Iterator<UrlParam> iterator() {
        return params.iterator();
    }

    public UrlParams add(String key, String value) {
        params.add(new UrlParam(key, value));
        return this;
    }

    public UrlParams add(UrlParam p) {
        params.add(p);
        return this;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }
}

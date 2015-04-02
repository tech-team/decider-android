package org.techteam.decider.net2;

import java.util.HashMap;
import java.util.Map;

public class Cookie {
    private String name;
    private String value;
    private Map<String, String> data = new HashMap<>();

    Cookie() {
    }

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(String name, String value, Map<String, String> data) {
        this.name = name;
        this.value = value;
        this.data = data;
    }

    void set(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public void addData(String key, String value) {
        this.data.put(key, value);
    }

    @Override
    public String toString() {
        return name + "=" + value + "; " + data;
    }
}

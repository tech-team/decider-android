package org.techteam.decider.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Headers implements Iterable<Headers.Header> {

    private List<Header> headers = new LinkedList<Header>();

    public Headers() {
    }

    @Override
    public Iterator<Header> iterator() {
        return headers.iterator();
    }

    public Headers add(String name, String value) {
        headers.add(new Header(name, value));
        return this;
    }

    public Headers add(Header h) {
        headers.add(h);
        return this;
    }


    public class Header {
        private String name;
        private String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}

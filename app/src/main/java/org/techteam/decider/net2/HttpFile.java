package org.techteam.decider.net2;

import java.io.InputStream;

public class HttpFile {
    private InputStream inputStream;
    private String filename;

    public HttpFile(InputStream inputStream, String filename) {
        this.inputStream = inputStream;
        this.filename = filename;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFilename() {
        return filename;
    }
}

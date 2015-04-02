package org.techteam.decider.net2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpResponse {
    private int responseCode;
    private String protocol;
    private Headers headers;
    private String body;
    private URL url;
    private CookieManager cookieManager;

    public HttpResponse(int responseCode, String protocol, Headers headers, String body, URL url) {
        this.responseCode = responseCode;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
        this.url = url;
    }

    public HttpResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Headers getHeaders() {
        return headers;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    void setCookies(Headers headers) throws IOException {
        cookieManager = new CookieManager();
        cookieManager.storeCookies(headers);
    }
}

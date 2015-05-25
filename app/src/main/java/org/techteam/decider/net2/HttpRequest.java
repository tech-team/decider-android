package org.techteam.decider.net2;

public class HttpRequest {
    public static enum Type {
        GET,
        POST,
        MULTIPART_POST
    }

    private static final String DEFAULT_ENCODING = "UTF-8";

    private String url;
    private UrlParams params;
    private Headers headers;
    private String encoding;
    private boolean followRedirects = true;
    private CookieManager cookieManager = null;
    private boolean saveCookies = false;
    private Type requestType = Type.GET;

    public HttpRequest(String url) {
        this(url, null, null);
    }

    public HttpRequest(String url, String encoding) {
        this(url, null, null, encoding);
    }

    public HttpRequest(String url, UrlParams params) {
        this(url, params, null, DEFAULT_ENCODING);
    }

    public HttpRequest(String url, UrlParams params, Headers headers) {
        this(url, params, headers, DEFAULT_ENCODING);
    }

    public HttpRequest(String url, UrlParams params, Headers headers, String encoding) {
        this.url = url;
        this.params = params;
        this.headers = headers;
        this.encoding = encoding;
    }

    public String getUrl() {
        return url;
    }

    public UrlParams getParams() {
        return params;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public boolean isSaveCookies() {
        return saveCookies;
    }

    public Type getRequestType() {
        return requestType;
    }

    public HttpRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpRequest setParams(UrlParams params) {
        this.params = params;
        return this;
    }

    public HttpRequest setHeaders(Headers headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public HttpRequest setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public HttpRequest setCookies(CookieManager cookies) {
        cookieManager = cookies;
        return this;
    }

    public void setSaveCookies(boolean saveCookies) {
        this.saveCookies = saveCookies;
    }

    public void setRequestType(Type requestType) {
        this.requestType = requestType;
    }
}

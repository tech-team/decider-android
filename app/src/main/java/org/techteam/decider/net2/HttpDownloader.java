package org.techteam.decider.net2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class HttpDownloader {
    // TODO: add proxy

    private static final String USER_AGENT = "Decider-App v0.1";


    public static HttpResponse httpGet(String url) throws IOException {
        HttpRequest req = new HttpRequest(url);
        return httpGet(req);
    }

    public static HttpResponse httpGet(String url, UrlParams params) throws IOException {
        HttpRequest req = new HttpRequest(url, params);
        return httpGet(req);
    }

    public static HttpResponse httpGet(HttpRequest request) throws IOException {
        URL urlObj = constructUrl(request.getUrl(), request.getParams(), request.getEncoding());
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) urlObj.openConnection();

            connection.setRequestMethod("GET");
            fillHeaders(request, connection);
            connection.setInstanceFollowRedirects(request.isFollowRedirects());
            connection.connect();

            return parseConnection(connection, request);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static HttpResponse httpPost(String url) throws IOException {
        HttpRequest req = new HttpRequest(url);
        return httpPost(req);
    }

    public static HttpResponse httpPost(HttpRequest request) throws IOException {
        URL urlObj = constructUrl(request.getUrl(), null, request.getEncoding());
        HttpURLConnection connection = null;
        OutputStream out = null;

        try {
            connection = (HttpURLConnection) urlObj.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(request.isFollowRedirects());
            fillHeaders(request, connection);

            connection.connect();

            String dataString = constructParams(request.getParams(), request.getEncoding());
            out = new BufferedOutputStream(connection.getOutputStream());
            out.write(dataString.getBytes());
            out.flush();

            return parseConnection(connection, request);
        } finally {
            if (out != null) {
                out.close();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static URL constructUrl(String url, UrlParams params, String encoding) throws MalformedURLException, UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return new URL(url);
        }
        return new URL(url + "?" + constructParams(params, encoding));
    }

    private static String constructParams(UrlParams params, String encoding) throws UnsupportedEncodingException {
        if (params == null) {
            return "";
        }

        String newUrl = "";
        for (UrlParams.UrlParam p : params) {
            newUrl += URLEncoder.encode(p.getKey(), encoding) + "=" + URLEncoder.encode(p.getValue(), encoding) + "&";
        }
        return newUrl.substring(0, newUrl.length() - 1);
    }

    private static void fillHeaders(HttpRequest request, HttpURLConnection connection) throws IOException {
        if (request != null) {
            CookieManager cm = request.getCookieManager();

            if (cm != null) {
                Headers.Header cookieHeader = cm.constructHeader(connection.getURL());
                connection.setRequestProperty(cookieHeader.getName(), cookieHeader.getValue());
            }

            Headers headers = request.getHeaders();
            if (headers != null) {
                for (Headers.Header h : headers) {
                    connection.setRequestProperty(h.getName(), h.getValuesSeparated());
                }
            }
        }
        connection.setRequestProperty("User-Agent", USER_AGENT);
    }


    private static String handleInputStream(InputStream in, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(encoding)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static HttpResponse parseConnection(HttpURLConnection connection, HttpRequest request) throws IOException {
        InputStream in = null;
        try {
            String protocol = null;
            String body = null;
            int status = connection.getResponseCode();
            Headers headers = new Headers();

            for (Map.Entry<String, List<String>> e : connection.getHeaderFields().entrySet()) {
                String key = e.getKey();
                if (key == null) {
                    protocol = connection.getHeaderField(null).split("\\s")[0];
                } else {
                    for (String v : e.getValue()) {
                        headers.add(key, v);
                    }
                }
            }

            if (status < 400) {
                in = connection.getInputStream();
            } else {
                in = connection.getErrorStream();
            }
            body = handleInputStream(in, request.getEncoding());
            URL url = connection.getURL();
            HttpResponse r = new HttpResponse(status, protocol, headers, body, url);
            if (request.isSaveCookies()) {
                r.setCookies(headers);
            }
            return r;
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        HttpRequest req = new HttpRequest("https://www.linkedin.com");

        HttpResponse r = HttpDownloader.httpGet(req);
        CookieManager cookieManager = r.getCookieManager();

    }
}

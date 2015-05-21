package org.techteam.decider.net2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class HttpDownloader {
    // TODO: add proxy

    private static final String USER_AGENT = "Decider-App v0.1";

    private static final String MULTIPART_BOUNDARY = "--DeciderBoundary";
    private static final char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();
    private static final int MULTIPART_BOUNDARY_MIN_SIZE = 30;
    private static final int MULTIPART_BOUNDARY_MAX_SIZE = 40;


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

    public static HttpResponse httpMultipartPost(HttpRequest request) throws IOException {
        URL urlObj = constructUrl(request.getUrl(), null, request.getEncoding());
        HttpURLConnection connection = null;
        OutputStream out = null;

        try {
            connection = (HttpURLConnection) urlObj.openConnection();

            String boundary = MULTIPART_BOUNDARY + generateNewBoundary();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setInstanceFollowRedirects(request.isFollowRedirects());

            fillHeaders(request, connection);

            connection.connect();
            out = new BufferedOutputStream(connection.getOutputStream());

            for (UrlParams.UrlParam<?> p : request.getParams()) {
                addBoundary(out, boundary);
                addPart(out, p);
                out.write("\r\n".getBytes());
            }
            addLastBoundary(out, boundary);

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

    private static void addBoundary(OutputStream out, String boundary) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
    }

    private static void addLastBoundary(OutputStream out, String boundary) throws IOException {
        out.write(("--" + boundary + "--\r\n").getBytes());
    }

    private static void addPart(OutputStream out, UrlParams.UrlParam<?> p) throws IOException {
        Class<?> c = p.getValue().getClass();
        if (c == String.class) {
            addPart(out, p.getKey(), (String) p.getValue());
        } else if (c == HttpFile.class) {
            addPart(out, p.getKey(), (HttpFile) p.getValue());
        } else {
            System.err.println("Unsupported UrlParam value type: " + c.toString());
        }
    }

    private static void addPart(OutputStream out, final String key, final String value) throws IOException {
        out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes());
        out.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
        out.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
        out.write(value.getBytes());
    }

    private static void addPart(OutputStream out, final String key, HttpFile httpFile) throws IOException {
        addPart(out, key, httpFile.getFilename(), httpFile.getInputStream());
    }

    private static void addPart(OutputStream out, final String key, final String filename, final InputStream fin) throws IOException {
        addPart(out, key, filename, fin, "application/octet-stream");
    }

    public static void addPart(OutputStream out, final String key, final String fileName, final InputStream fin, String type) throws IOException {
        type = "Content-Type: " + type + "\r\n";
        out.write(("Content-Disposition: form-data; name=\""+ key+"\"; filename=\"" + fileName + "\"\r\n").getBytes());
        out.write(type.getBytes());
        out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

        final byte[] tmp = new byte[4096];
        int l = 0;
        while ((l = fin.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }
    }

    private static String generateNewBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(MULTIPART_BOUNDARY_MAX_SIZE - MULTIPART_BOUNDARY_MIN_SIZE + 1) + MULTIPART_BOUNDARY_MIN_SIZE;
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
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
        for (UrlParams.UrlParam<?> p : params) {
            newUrl += URLEncoder.encode(p.getKey(), encoding) + "=" + URLEncoder.encode((String) p.getValue(), encoding) + "&";
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

    }
}

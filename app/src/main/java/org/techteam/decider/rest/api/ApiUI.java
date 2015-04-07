package org.techteam.decider.rest.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.net2.HttpDownloader;
import org.techteam.decider.net2.HttpRequest;
import org.techteam.decider.net2.HttpResponse;
import org.techteam.decider.net2.UrlParams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class ApiUI {

    private Context context;
    private SharedPreferences prefs;

//    private static final String API_URL = "http://localhost:8888/api/v1";
    private static final String API_URL = "http://private-3225b-decider.apiary-mock.com/";

    public ApiUI(Context context) {
        String prefName = "TOKENS_PREFS"; // TODO
        this.context = context;
        prefs = this.context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public String getAccessToken() {
        String key = "access_token"; // TODO
        return prefs.getString(key, "ACCESS_TOKEN");
    }

    public String getRefreshToken() {
        String key = "refresh_token"; // TODO
        return prefs.getString(key, null);
    }

    public String getExpirationDate() {
        String keyExpires = "expires"; // TODO
        String keyAddedDate = "got_date"; // TODO
//        return prefs.getString(key, null);
        return null;
    }

    public HttpResponse makeProtectedGetCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));

        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new InvalidAccessTokenException();
        }
        params.add("access_token", accessToken);
        httpRequest.setParams(params);

        HttpResponse httpResponse = HttpDownloader.httpGet(httpRequest);
        int code = httpResponse.getResponseCode();

        if (code == HttpURLConnection.HTTP_FORBIDDEN) {
            refreshToken();
            httpResponse = HttpDownloader.httpGet(httpRequest);
            code = httpResponse.getResponseCode();
            if (code != HttpURLConnection.HTTP_FORBIDDEN) {
                return httpResponse;
            }
            throw new InvalidAccessTokenException();
        }

        return httpResponse;
    }

    public JSONObject getQuestions(GetQuestionsRequest request) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        UrlParams params = new UrlParams();
        params.add("limit", request.getLimit());
        params.add("offset", request.getOffset());
        params.add("tab", request.getContentSection().toString().toLowerCase());

        HttpResponse response = makeProtectedGetCall(GetQuestionsRequest.URL, params);
        if (response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    private void refreshToken() throws IOException, JSONException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(AuthRequest.REFRESH_TOKEN_URL));
        UrlParams params = new UrlParams();
        params.add("refresh_token", getRefreshToken());
        HttpResponse response = HttpDownloader.httpPost(httpRequest);
        if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String body = response.getBody();
            JSONObject resp = new JSONObject(body);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("access_token", resp.getString("access_token"));
            editor.putString("expires", resp.getString("expires"));
            editor.putString("refresh_token", resp.getString("refresh_token"));
            editor.apply();
        }
        throw new TokenRefreshFailException();
    }

    private String resolveApiUrl(String path) {
        URI uri;
        try {
            uri = new URI(API_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return uri.resolve(path).toString();
    }
}

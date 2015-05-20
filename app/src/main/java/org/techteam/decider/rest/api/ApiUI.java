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

//    private static final String API_URL = "http://localhost:8888/api/v1/";
//    private static final String API_URL = "http://188.166.126.79/api/v1/";
    private static final String API_URL = "http://decidr.ru/api/v1/";
//    private static final String API_URL = "http://private-954f0e-decider.apiary-mock.com/";


    public static final String REFRESH_TOKEN_URL = "refresh_token";

    public ApiUI(Context context) {
        String prefName = "TOKENS_PREFS"; // TODO
        this.context = context;
        prefs = this.context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
//        saveToken("0atkeWZqnRq9GVxHNCCtJpOevUtrrN", 36000, "Z77gke1xm6xJ5xaAs7mTUB9fSm1RpO");
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
        if (code == HttpURLConnection.HTTP_OK) {
            return httpResponse;
        }
        return null;
    }

    public HttpResponse makeProtectedPostCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));

        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new InvalidAccessTokenException("No access token found");
        }
        params.add("access_token", accessToken);
        httpRequest.setParams(params);

        HttpResponse httpResponse = HttpDownloader.httpPost(httpRequest);
        int code = httpResponse.getResponseCode();

        if (code == HttpURLConnection.HTTP_FORBIDDEN) {
            refreshToken();
            httpResponse = HttpDownloader.httpPost(httpRequest);
            code = httpResponse.getResponseCode();
            if (code != HttpURLConnection.HTTP_FORBIDDEN) {
                return httpResponse;
            }
            throw new InvalidAccessTokenException("Access token is probably expired");
        }
        return httpResponse;
//        if (code == HttpURLConnection.HTTP_OK) {
//        }
//        return null;
    }

    public HttpResponse makeAuthPostCall(String url, UrlParams params) throws IOException, JSONException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));
        httpRequest.setParams(params);

        HttpResponse httpResponse = HttpDownloader.httpPost(httpRequest);
        int code = httpResponse.getResponseCode();

        if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
            return httpResponse;
        }
        return null;
    }

    public JSONObject getQuestions(GetQuestionsRequest request) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        UrlParams params = new UrlParams();
        params.add("limit", request.getLimit());
        params.add("offset", request.getOffset());
        params.add("tab", request.getContentSection().toString().toLowerCase());
        for (int category : request.getCategories()) {
            params.add("categories[]", category);
        }

        HttpResponse response = makeProtectedGetCall(GetQuestionsRequest.URL, params);
        if (response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject getCategories(GetCategoriesRequest request) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        UrlParams params = new UrlParams();
        params.add("locale", request.getLocale());

        HttpResponse response = makeProtectedGetCall(GetCategoriesRequest.URL, params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject loginRegister(RegisterRequest request) throws IOException, JSONException {
        UrlParams params = new UrlParams();
        params.add("email", request.getEmail());
        params.add("password", request.getPassword());

        HttpResponse response = makeAuthPostCall(RegisterRequest.URL, params);
        if (response.getBody() == null) {
            return null;
        }
        JSONObject obj = new JSONObject(response.getBody());
        JSONObject objData = obj.getJSONObject("data");
        saveToken(objData);
        return obj;
    }

    public JSONObject createQuestion(CreateQuestionRequest request) throws JSONException, TokenRefreshFailException, IOException, InvalidAccessTokenException {
        UrlParams params = new UrlParams();
        params.add("data", request.getQuestionDataJson());

        HttpResponse response = makeProtectedPostCall(CreateQuestionRequest.URL, params);
        if (response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject uploadImage(UploadImageRequest request) throws JSONException, TokenRefreshFailException, IOException, InvalidAccessTokenException {
        // TODO
//        UrlParams params = new UrlParams();
//        params.add("data", request.getQuestionDataJson());

//        HttpResponse response = makeProtectedPostCall(CreateQuestionRequest.URL, params);
//        if (response.getBody() == null) {
//            return null;
//        }
//        return new JSONObject(response.getBody());
        return null;
    }

    private void saveToken(String accessToken, int expires, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", accessToken);
        editor.putInt("expires", expires);
        editor.putString("refresh_token", refreshToken);
        editor.apply();
    }

    private void saveToken(JSONObject objData) throws JSONException {
        saveToken(objData.getString("access_token"), objData.getInt("expires"), objData.getString("refresh_token"));
    }

    private void refreshToken() throws IOException, JSONException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(REFRESH_TOKEN_URL));
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

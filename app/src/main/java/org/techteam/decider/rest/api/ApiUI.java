package org.techteam.decider.rest.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.net2.HttpDownloader;
import org.techteam.decider.net2.HttpFile;
import org.techteam.decider.net2.HttpRequest;
import org.techteam.decider.net2.HttpResponse;
import org.techteam.decider.net2.UrlParams;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class ApiUI {

    private Context context;
    private SharedPreferences prefs;

//    private static final String API_URL = "http://localhost:8888/api/v1/";
//    private static final String API_URL = "http://private-954f0e-decider.apiary-mock.com/";

    public static final String BASE_URL = "http://decidr.ru/";
    public static final URI BASE_URI;
    public static final String API_PATH = "api/v1/";
    private static final URI API_URL;

    private static final String REFRESH_TOKEN_PATH = "refresh_token";

    static {
        URI uri;
        try {
            BASE_URI = new URI(BASE_URL);
            uri = new URI(BASE_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        API_URL = uri.resolve(API_PATH);
    }

    private class PrefsKeys {
        public static final String PREFS_NAME = "TOKENS_PREFS";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String EXPIRES = "expires";
    }

    public ApiUI(Context context) {
        this.context = context;
        prefs = this.context.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE);
//        saveToken("0atkeWZqnRq9GVxHNCCtJpOevUtrrN", 36000, "Z77gke1xm6xJ5xaAs7mTUB9fSm1RpO");
    }

    public String getAccessToken() {
        return prefs.getString(PrefsKeys.ACCESS_TOKEN, "ACCESS_TOKEN");
    }

    public String getRefreshToken() {
        return prefs.getString(PrefsKeys.REFRESH_TOKEN, "REFRESH_TOKEN");
    }

    public int getExpires() {
        return prefs.getInt(PrefsKeys.EXPIRES, 0);
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
        if (response == null || response.getBody() == null) {
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
        if (response == null || response.getBody() == null) {
            return null;
        }
        int code = response.getResponseCode();

        JSONObject obj = new JSONObject(response.getBody());
        if (code < 400) {
            JSONObject objData = obj.getJSONObject("data");
            saveToken(objData);
        }
        return obj;
    }

    public JSONObject createQuestion(CreateQuestionRequest request) throws JSONException, TokenRefreshFailException, IOException, InvalidAccessTokenException {
        QuestionData data = request.getQuestionData();
        UrlParams params = new UrlParams();
        params.add("data", data.toJson());

        HttpResponse response = makeProtectedPostCall(CreateQuestionRequest.URL, params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject uploadImage(UploadImageRequest request) throws JSONException, TokenRefreshFailException, IOException, InvalidAccessTokenException {
        UrlParams params = new UrlParams();
        UploadImageRequest.Image image = request.getImage();

        FileInputStream originalFin = null;
        BufferedInputStream originalBufin = null;

        FileInputStream previewFin = null;
        BufferedInputStream previewBufin = null;

        try {
            File originalFile = new File(image.getOriginalFilename());
            originalFin = new FileInputStream(originalFile);
            originalBufin = new BufferedInputStream(originalFin);

            File previewFile = new File(image.getPreviewFilename());
            previewFin = new FileInputStream(previewFile);
            previewBufin = new BufferedInputStream(previewFin);

            params.add("image", new HttpFile(originalBufin, originalFile.getName()));
            params.add("preview", new HttpFile(previewBufin, previewFile.getName()));

            HttpResponse response = makeProtectedMultipartPostCall(UploadImageRequest.URL, params);
            if (response == null || response.getBody() == null) {
                return null;
            }
            return new JSONObject(response.getBody());
        } finally {
            if (originalBufin != null) {
                originalBufin.close();
            }
            if (originalFin != null) {
                originalFin.close();
            }

            if (previewBufin != null) {
                previewBufin.close();
            }
            if (previewFin != null) {
                previewFin.close();
            }
        }
    }

    private HttpRequest prepareHttpRequest(HttpRequest httpRequest, UrlParams params) throws InvalidAccessTokenException {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new InvalidAccessTokenException();
        }
        params.add("access_token", accessToken);
        httpRequest.setParams(params);
        return httpRequest;
    }

    private HttpResponse checkResponse(HttpResponse httpResponse, HttpRequest httpRequest) throws JSONException, TokenRefreshFailException, IOException, InvalidAccessTokenException {
        if (httpResponse == null) {
            return null;
        }

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

    private HttpResponse makeProtectedGetCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));
        prepareHttpRequest(httpRequest, params);

        HttpResponse httpResponse = HttpDownloader.httpGet(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }

    private HttpResponse makeProtectedPostCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));
        prepareHttpRequest(httpRequest, params);

        HttpResponse httpResponse = HttpDownloader.httpPost(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }

    private HttpResponse makeProtectedMultipartPostCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));
        prepareHttpRequest(httpRequest, params);

        HttpResponse httpResponse = HttpDownloader.httpMultipartPost(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }


    private HttpResponse makeAuthPostCall(String url, UrlParams params) throws IOException, JSONException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(url));
        httpRequest.setParams(params);

        HttpResponse httpResponse = HttpDownloader.httpPost(httpRequest);
        int code = httpResponse.getResponseCode();

        return httpResponse;
    }

    private void saveToken(String accessToken, int expires, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsKeys.ACCESS_TOKEN, accessToken);
        editor.putInt(PrefsKeys.EXPIRES, expires);
        editor.putString(PrefsKeys.REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    private void saveToken(JSONObject objData) throws JSONException {
        saveToken(objData.getString("access_token"), objData.getInt("expires"), objData.getString("refresh_token"));
    }

    private void refreshToken() throws IOException, JSONException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(REFRESH_TOKEN_PATH));
        UrlParams params = new UrlParams();
        params.add("refresh_token", getRefreshToken());
        HttpResponse response = HttpDownloader.httpPost(httpRequest);
        if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String body = response.getBody();
            JSONObject resp = new JSONObject(body);
            JSONObject data = resp.getJSONObject("data");
            saveToken(data);
        } else {
            throw new TokenRefreshFailException("Response code = " + response.getResponseCode());
        }
    }

    private String resolveApiUrl(String path) {
        return API_URL.resolve(path).toString();
    }

    public static String resolveUrl(String path) {
        return BASE_URI.resolve(path).toString();
    }
}

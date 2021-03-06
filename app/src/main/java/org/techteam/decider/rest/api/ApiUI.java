package org.techteam.decider.rest.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.auth.AccountGeneral;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.content.UserData;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.content.question.ImageQuestionData;
import org.techteam.decider.net2.HttpDownloader;
import org.techteam.decider.net2.HttpFile;
import org.techteam.decider.net2.HttpRequest;
import org.techteam.decider.net2.HttpResponse;
import org.techteam.decider.net2.UrlParams;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

public class ApiUI {

    private static final String TAG = ApiUI.class.getName();
    private Context context;
    private SharedPreferences prefs;

    public static final String BASE_URL = "http://decidr.ru/";
    public static final Uri BASE_URI;
    private static final Uri API_URI;
    private static final Uri PUSH_URI;
    private static final Uri SHARE_IMAGE_URI;

    private static final String REFRESH_TOKEN_PATH = "refresh_token";

    static {
        BASE_URI = Uri.parse(BASE_URL);
        API_URI = BASE_URI.buildUpon().appendEncodedPath("api/v1/").build();
        PUSH_URI = BASE_URI.buildUpon().appendEncodedPath("push/").build();
        SHARE_IMAGE_URI = BASE_URI.buildUpon().appendEncodedPath("question/").build();
    }

    private class PrefsKeys {
        public static final String PREFS_NAME = "TOKENS_PREFS";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String EXPIRES = "expires";
        public static final String CURRENT_USER = "current_user";
    }

    public ApiUI(Context context) {
        this.context = context;

        prefs = this.context.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getAccessToken() throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManager am = AccountManager.get(this.context);
        Account[] accounts = am.getAccountsByType(context.getApplicationContext().getPackageName());
        if (accounts.length == 0) {
            return null;
        }

        AccountManagerFuture<Bundle> f = am.getAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null);
        Bundle res = f.getResult();
        return res.getString(AccountManager.KEY_AUTHTOKEN);
    }

    public static String getCurrentUserId(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(context.getApplicationContext().getPackageName());
        if (accounts.length == 0) {
            return null;
        }
        return am.getUserData(accounts[0], ServiceCallback.LoginRegisterExtras.USER_ID);
    }

    public JSONObject getQuestions(QuestionsGetRequest request) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("first_question_id", request.getFirstQuestionId());
        params.add("limit", request.getLimit());
        params.add("offset", request.getOffset());
        params.add("tab", request.getContentSection().toString().toLowerCase());
        for (int category : request.getCategories()) {
            params.add("categories[]", category);
        }

        HttpResponse response = makeProtectedGetCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject getCategories(CategoriesGetRequest request) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("locale", request.getLocale());

        HttpResponse response = makeProtectedGetCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject loginRegister(OperationType op, LoginRegisterRequest request) throws IOException, JSONException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("email", request.getEmail());
        params.add("password", request.getPassword());

        String path;
        switch (op) {
            case LOGIN:
                path = LoginRegisterRequest.LOGIN_URL;
                break;
            case REGISTER:
                path = LoginRegisterRequest.REGISTER_URL;
                break;
            default:
                throw new RuntimeException(new IllegalArgumentException("unexpected operationType on loginRegister"));
        }
        HttpResponse response = makePostCall(resolveApiUrl(path), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        int code = response.getResponseCode();

        if (code >= 500) {
            throw new ServerErrorException(code);
        }

        JSONObject obj = new JSONObject(response.getBody());
        if (code < 400) {
            JSONObject objData = obj.getJSONObject("data");
//            saveToken(objData);
        }
        return obj;
    }

    public JSONObject createQuestion(QuestionCreateRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        ImageQuestionData q = (ImageQuestionData) request.getQuestionData();
        ImageData[] images = { q.getPicture1(), q.getPicture2() };
        ImageParamFacade[] imageParamFacades = new ImageParamFacade[images.length];
        for (int i = 0; i < images.length; ++i) {
            imageParamFacades[i] = new ImageParamFacade(images[i]);
        }

        UrlParams params = new UrlParams();
        params.add("text", q.getText());
        params.add("category_id", Integer.toString(q.getCategoryEntryUid()));
        params.add("items_count", Integer.toString(images.length));
        params.add("is_anonymous", Boolean.toString(q.isAnonymous()));

        try {
            for (int i = 0; i < imageParamFacades.length; ++i) {
                String pollStr = "poll_" + Integer.toString(i + 1);
                String textPicKey = pollStr + "_text";
                String originalPicKey = pollStr + "_image";
                String previewPicKey = pollStr + "_preview";

                params.add(textPicKey, "DUMMY");
                imageParamFacades[i].write(params, originalPicKey, previewPicKey);
            }

            HttpResponse response = makeProtectedMultipartPostCall(resolveApiUrl(request.getPath()), params);
            if (response == null || response.getBody() == null) {
                return null;
            }
            return new JSONObject(response.getBody());
        } finally {
            for (ImageParamFacade imageParamFacade : imageParamFacades) {
                imageParamFacade.close();
            }
        }
    }

    public JSONObject createComment(CommentCreateRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        CommentData data = request.getCommentData();
        UrlParams params = new UrlParams();
        params.add("data", data.toJson().toString());

        HttpResponse response = makeProtectedPostCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject uploadImage(ImageUploadRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        ImageData image = request.getImage();
        ImageParamFacade imageParamFacade = new ImageParamFacade(image);

        try {
            imageParamFacade.write(params, "image", "preview");

            HttpResponse response = makeProtectedMultipartPostCall(resolveApiUrl(request.getPath()), params);
            if (response == null || response.getBody() == null) {
                return null;
            }
            return new JSONObject(response.getBody());
        } finally {
            imageParamFacade.close();
        }
    }

    public JSONObject pollVote(PollVoteRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("question_id", request.getQuestionId());
        params.add("poll_item_id", request.getPollItemId());

        HttpResponse response = makeProtectedPostCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject entityVote(EntityVoteRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("entity", request.getEntityType());
        params.add("entity_id", request.getEntityId());

        HttpResponse response = makeProtectedPostCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject getComments(CommentsGetRequest request) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("question_id", request.getQuestionId());
        params.add("limit", request.getLimit());
        params.add("offset", request.getOffset());

        HttpResponse response = makeProtectedGetCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject getUser(UserGetRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("user_id", request.getUserId());

        if (request.getAccessToken() != null) {
            params.add("access_token", request.getAccessToken());
        }

        HttpResponse response = makeProtectedGetCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject editUser(UserEditRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UserData userData = request.getUserData();
        String accessToken = request.getAccessToken();

        ImageParamFacade imageParamFacade = null;
        try {
            UrlParams params = new UrlParams();
            if (userData.hasUsername()) params.add("username", userData.getUsername());
            if (userData.hasFirstName()) params.add("first_name", userData.getFirstName());
            if (userData.hasLastName()) params.add("last_name", userData.getLastName());
            if (userData.hasGender()) params.add("gender", userData.getGender().getLetter());
            if (userData.hasBirthday()) params.add("birthday", userData.getBirthday());
            if (userData.hasCountry()) params.add("country", userData.getCountry());
            if (userData.hasCity()) params.add("city", userData.getCity());
            if (userData.hasAbout()) params.add("about", userData.getAbout());
            if (userData.hasAvatar()) {
                imageParamFacade = new ImageParamFacade(userData.getAvatar());
                imageParamFacade.write(params, null, "avatar");
            }

            if (accessToken != null) {
                params.add("access_token", accessToken);
            }

            HttpResponse response = makeProtectedMultipartPostCall(resolveApiUrl(request.getPath()), params);
            if (response == null || response.getBody() == null) {
                return null;
            }
            return new JSONObject(response.getBody());
        } finally {
            if (imageParamFacade != null) {
                imageParamFacade.close();
            }
        }
    }

    public JSONObject authPush(PushAuthRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("instance_id", request.getInstanceId());
        params.add("reg_token", request.getRegToken());

        HttpResponse response = makeProtectedPostCall(resolvePushUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    public JSONObject reportSpam(ReportSpamRequest request) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        UrlParams params = new UrlParams();
        params.add("entity", request.getEntityType());
        params.add("entity_id", request.getEntityId());

        HttpResponse response = makeProtectedPostCall(resolveApiUrl(request.getPath()), params);
        if (response == null || response.getBody() == null) {
            return null;
        }
        return new JSONObject(response.getBody());
    }

    private HttpRequest prepareHttpRequest(HttpRequest httpRequest, UrlParams params) throws InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, IOException {
        if (params.get("access_token") == null) {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                throw new InvalidAccessTokenException();
            }
            params.add("access_token", accessToken);
        }
        httpRequest.setParams(params);
        return httpRequest;
    }

    private HttpResponse checkResponse(HttpResponse httpResponse, HttpRequest httpRequest) throws JSONException, IOException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        if (httpResponse == null) {
            return null;
        }

        int code = httpResponse.getResponseCode();

        if (code >= 500) {
            throw new ServerErrorException(code);
        }

        if (code == HttpURLConnection.HTTP_FORBIDDEN) {
            String oldAccessToken = getAccessToken();
            AccountManager am = AccountManager.get(context);
            am.invalidateAuthToken(context.getApplicationContext().getPackageName(), oldAccessToken);
            String newAccessToken = getAccessToken();
            if (newAccessToken == null) {
                Account[] accounts = am.getAccountsByType(context.getApplicationContext().getPackageName());
                if (accounts.length != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        am.removeAccountExplicitly(accounts[0]);
                    } else {
                        AccountManagerFuture<Boolean> f = am.removeAccount(accounts[0], null, null);
                        f.getResult();
                    }
                }
                throw new InvalidAccessTokenException();
            }

            httpRequest.getParams().replace("access_token", newAccessToken);
            httpResponse = HttpDownloader.exec(httpRequest);
            code = httpResponse.getResponseCode();
            if (code != HttpURLConnection.HTTP_FORBIDDEN) {
                return httpResponse;
            }
            throw new InvalidAccessTokenException(httpResponse.getBody());
        }
        return httpResponse;
    }

    private HttpResponse makeProtectedGetCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        HttpRequest httpRequest = new HttpRequest(url);
        prepareHttpRequest(httpRequest, params);
        httpRequest.setRequestType(HttpRequest.Type.GET);

        HttpResponse httpResponse = HttpDownloader.exec(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }

    private HttpResponse makeProtectedPostCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        HttpRequest httpRequest = new HttpRequest(url);
        prepareHttpRequest(httpRequest, params);
        httpRequest.setRequestType(HttpRequest.Type.POST);

        HttpResponse httpResponse = HttpDownloader.exec(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }

    private HttpResponse makeProtectedMultipartPostCall(String url, UrlParams params) throws IOException, JSONException, InvalidAccessTokenException, AuthenticatorException, OperationCanceledException, ServerErrorException {
        HttpRequest httpRequest = new HttpRequest(url);
        prepareHttpRequest(httpRequest, params);
        httpRequest.setRequestType(HttpRequest.Type.MULTIPART_POST);

        HttpResponse httpResponse = HttpDownloader.exec(httpRequest);
        httpResponse = checkResponse(httpResponse, httpRequest);
        return httpResponse;
    }


    private HttpResponse makePostCall(String url, UrlParams params) throws IOException, JSONException {
        HttpRequest httpRequest = new HttpRequest(url);
        httpRequest.setParams(params);
        httpRequest.setRequestType(HttpRequest.Type.POST);

        HttpResponse httpResponse = HttpDownloader.exec(httpRequest);
        int code = httpResponse.getResponseCode();

        return httpResponse;
    }

    public String extractToken(JSONObject objData) throws JSONException {
        return objData.getString("access_token");
    }

    public int extractTokenExpires(JSONObject objData) throws JSONException {
        return objData.getInt("expires");
    }

    public String extractRefreshToken(JSONObject objData) throws JSONException {
        return objData.getString("refresh_token");
    }

    public JSONObject refreshToken(String refreshToken) throws IOException, JSONException, TokenRefreshFailException {
        HttpRequest httpRequest = new HttpRequest(resolveApiUrl(REFRESH_TOKEN_PATH));
        UrlParams params = new UrlParams();
        params.add("refresh_token", refreshToken);
        httpRequest.setParams(params);
        HttpResponse response = HttpDownloader.httpPost(httpRequest);

        String body = response.getBody();
        if (response.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            JSONObject resp = new JSONObject(body);
            JSONObject data = resp.getJSONObject("data");
            return data;
//            saveToken(data);
        } else {
            throw new TokenRefreshFailException("Response code = " + response.getResponseCode() + "; response = " + body + "; refreshToken = " + refreshToken);
        }
    }


    public static String resolveApiUrl(String... paths) {
        Uri.Builder uri = API_URI.buildUpon();
        for (String path : paths) {
            uri = uri.appendEncodedPath(path);
        }
        return uri.build().toString();
    }

    public static String resolvePushUrl(String... paths) {
        Uri.Builder uri = PUSH_URI.buildUpon();
        for (String path : paths) {
            uri = uri.appendEncodedPath(path);
        }
        return uri.build().toString();
    }

    public static String resolveUrl(String... paths) {
        Uri.Builder uri = BASE_URI.buildUpon();
        for (String path : paths) {
            uri = uri.appendEncodedPath(path);
        }
        return uri.build().toString();
    }

    public static String resolveShareImageUrl(QuestionEntry entry) {
        String qid = String.valueOf(entry.getQId());
        return SHARE_IMAGE_URI.buildUpon().appendEncodedPath(qid).build().toString();
    }



    private static class ImageParamFacade {
        private final ImageData imageData;

        FileInputStream originalFIS = null;
        BufferedInputStream originalBIS = null;

        FileInputStream previewFIS = null;
        BufferedInputStream previewBIS = null;

        File originalFile = null;
        File previewFile = null;

        public ImageParamFacade(ImageData imageData) {
            this.imageData = imageData;
        }

        public void write(UrlParams params, String originalKey, String previewKey) throws FileNotFoundException {
            if (originalKey != null) {
                originalFile = new File(imageData.getOriginalFilename());
                originalFIS = new FileInputStream(originalFile);
                originalBIS = new BufferedInputStream(originalFIS);
                params.add(originalKey, new HttpFile(originalBIS, originalFile.getName()));
            }

            if (previewKey != null) {
                previewFile = new File(imageData.getPreviewFilename());
                previewFIS = new FileInputStream(previewFile);
                previewBIS = new BufferedInputStream(previewFIS);
                params.add(previewKey, new HttpFile(previewBIS, previewFile.getName()));
            }
        }

        public void close() throws IOException {
            if (originalBIS != null) {
                originalBIS.close();
            }
            if (originalFIS != null) {
                originalFIS.close();
            }

            if (previewBIS != null) {
                previewBIS.close();
            }
            if (previewFIS != null) {
                previewFIS.close();
            }
        }
    }
}

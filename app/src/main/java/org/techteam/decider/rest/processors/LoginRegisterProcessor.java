package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.LoginRegisterRequest;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class LoginRegisterProcessor extends RequestProcessor<LoginRegisterRequest> {
    private static final String TAG = LoginRegisterProcessor.class.getName();

    public LoginRegisterProcessor(Context context, LoginRegisterRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return null;
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        JSONObject userJson = data.getJSONObject("user");

        String uid = null;
        String username = null;

        ActiveAndroid.beginTransaction();
        try {
            UserEntry entry = UserEntry.fromJson(userJson);
            entry.save();
            ActiveAndroid.setTransactionSuccessful();

            uid = entry.getUid();
            username = entry.getUsername();
        } finally {
            ActiveAndroid.endTransaction();
        }

        result.putString(ServiceCallback.LoginRegisterExtras.USERNAME, username);
        result.putString(ServiceCallback.LoginRegisterExtras.TOKEN, apiUI.extractToken(data));
        result.putLong(ServiceCallback.LoginRegisterExtras.EXPIRES, System.currentTimeMillis() + apiUI.extractTokenExpires(data) * 1000);
        result.putString(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, apiUI.extractRefreshToken(data));
        result.putString(ServiceCallback.LoginRegisterExtras.USER_ID, uid);
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {
        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.loginRegister(operationType, getRequest());
            Log.i(TAG, response.toString());


            if (!checkResponse(response, operationType, requestId, result, cb)) {
                return;
            }

            postExecute(response, result);
            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        } catch (ServerErrorException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            result.putInt(ServiceCallback.ErrorsExtras.GENERIC_ERROR_CODE, ServiceCallback.ErrorsExtras.GenericErrors.SERVER_ERROR);
            result.putInt(ServiceCallback.ErrorsExtras.INTERNAL_SERVER_ERROR, e.getCode());
            cb.onError(e.getMessage(), result);
        }
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ServiceCallback.LoginRegisterExtras.LOGIN, getRequest().getEmail());
        bundle.putString(ServiceCallback.LoginRegisterExtras.PASSWORD, getRequest().getPassword());

        return bundle;
    }
}

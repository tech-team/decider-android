package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.LoginRegisterRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class LoginRegisterProcessor extends Processor {
    private static final String TAG = LoginRegisterProcessor.class.getName();
    private final LoginRegisterRequest request;

    public LoginRegisterProcessor(Context context, LoginRegisterRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {
        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.loginRegister(operationType, request);
            Log.i(TAG, response.toString());


            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            JSONObject data = response.getJSONObject("data");
            JSONObject userJson = data.getJSONObject("user");

            ActiveAndroid.beginTransaction();
            try {
                UserEntry entry = UserEntry.fromJson(userJson, true);
                entry.save();
                ActiveAndroid.setTransactionSuccessful();

                apiUI.setCurrentUserId(entry.getUid());
            } finally {
                ActiveAndroid.endTransaction();
            }
            transactionFinished(operationType, requestId);

            result.putString(ServiceCallback.LoginRegisterExtras.TOKEN, apiUI.extractToken(data));
            result.putLong(ServiceCallback.LoginRegisterExtras.EXPIRES, System.currentTimeMillis() + apiUI.extractTokenExpires(data) * 1000);
            result.putString(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, apiUI.extractRefreshToken(data));

            cb.onSuccess(result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        }
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ServiceCallback.LoginRegisterExtras.LOGIN, request.getEmail());
        bundle.putString(ServiceCallback.LoginRegisterExtras.PASSWORD, request.getPassword());

        return bundle;
    }
}

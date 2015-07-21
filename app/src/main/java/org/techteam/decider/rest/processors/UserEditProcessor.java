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
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.UserEditRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class UserEditProcessor extends RequestProcessor<UserEditRequest> {
    private static final String TAG = UserEditProcessor.class.getName();

    public UserEditProcessor(Context context, UserEditRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.editUser(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        ActiveAndroid.beginTransaction();
        try {
            UserEntry entry = UserEntry.fromJson(data);
            entry.save();
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    public void postExecuteError(JSONObject response, int errorCode, Bundle result) throws JSONException {
        super.postExecuteError(response, errorCode, result);

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putString(ServiceCallback.EditUserExtras.USERNAME, getRequest().getUserData().getUsername());
        return data;
    }
}

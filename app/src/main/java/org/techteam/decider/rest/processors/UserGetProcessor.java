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
import org.techteam.decider.rest.api.UserGetRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class UserGetProcessor extends RequestProcessor<UserGetRequest> {
    private static final String TAG = UserGetProcessor.class.getName();

    public UserGetProcessor(Context context, UserGetRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.getUser(getRequest());
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
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putString(ServiceCallback.GetUserExtras.USER_ID, getRequest().getUserId());
        return data;
    }
}

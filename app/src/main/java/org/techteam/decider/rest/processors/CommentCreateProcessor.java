package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CommentCreateRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class CommentCreateProcessor extends RequestProcessor<CommentCreateRequest> {
    private static final String TAG = CommentCreateProcessor.class.getName();

    public CommentCreateProcessor(Context context, CommentCreateRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.createComment(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONArray data = response.getJSONArray("data");
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < data.length(); ++i) {
                CommentEntry entry = CommentEntry.fromJson(data.getJSONObject(i));
                entry.saveTotal();
            }
            ActiveAndroid.setTransactionSuccessful();

        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.GetCommentsExtras.QUESTION_ID, getRequest().getCommentData().getQuestionId());
        return data;
    }
}

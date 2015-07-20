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
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CommentsGetRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class CommentsGetProcessor extends RequestProcessor<CommentsGetRequest> {
    private static final String TAG = CommentsGetProcessor.class.getName();

    public CommentsGetProcessor(Context context, CommentsGetRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.getComments(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        if (getRequest().getLoadIntention() == LoadIntention.REFRESH) {
            CommentEntry.deleteAll();
        }

        JSONObject data = response.getJSONObject("data");
        int remainig = data.getInt("remaining"); // TODO
        JSONArray comments = data.getJSONArray("comments");

        if (comments.length() == 0) {
            result.putBoolean(ServiceCallback.GetCommentsExtras.FEED_FINISHED, true);
        } else {

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < comments.length(); ++i) {
                    JSONObject q = comments.getJSONObject(i);
                    CommentEntry entry = CommentEntry.fromJson(q);
                    entry.saveTotal();
                }
                ActiveAndroid.setTransactionSuccessful();

                result.putInt(ServiceCallback.GetCommentsExtras.COUNT, data.length());
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.GetCommentsExtras.LOAD_INTENTION, getRequest().getLoadIntention());
        data.putInt(ServiceCallback.GetCommentsExtras.QUESTION_ID, getRequest().getQuestionId());
        return data;
    }
}

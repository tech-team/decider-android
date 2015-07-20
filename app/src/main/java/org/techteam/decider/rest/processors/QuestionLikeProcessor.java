package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.QuestionLikeRequest;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class QuestionLikeProcessor extends RequestProcessor<QuestionLikeRequest> {
    private static final String TAG = QuestionLikeProcessor.class.getName();

    public QuestionLikeProcessor(Context context, QuestionLikeRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.entityVote(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        int entityId = data.getInt("entity_id");
        int likesCount = data.getInt("likes_count");
        boolean voted = data.has("voted") ? data.getBoolean("voted") : true;

        ActiveAndroid.beginTransaction();
        try {
            QuestionEntry entry = QuestionEntry.byQId(entityId);
            entry.likesCount = likesCount;
            entry.voted = voted;
            entry.save();
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        result.putInt(ServiceCallback.EntityVoteExtras.ENTITY_ID, entityId);
        result.putInt(ServiceCallback.EntityVoteExtras.VOTES_COUNT, likesCount);
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.EntityVoteExtras.ENTRY_POSITION, getRequest().getEntryPosition());
        return data;
    }
}

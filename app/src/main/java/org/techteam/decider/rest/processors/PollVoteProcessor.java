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
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.PollVoteRequest;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class PollVoteProcessor extends RequestProcessor<PollVoteRequest> {
    private static final String TAG = PollVoteProcessor.class.getName();

    public PollVoteProcessor(Context context, PollVoteRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.pollVote(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONArray data = response.getJSONArray("data");

        int requestedItemVotesCount = -1;

        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < data.length(); ++i) {
                JSONObject item = data.getJSONObject(i);
                int pollItemId = item.getInt("poll_item_id");
                int votesCount = item.getInt("votes_count");
                boolean voted = item.has("voted") ? item.getBoolean("voted") : false;
                if (pollItemId == getRequest().getPollItemId()) {
                    requestedItemVotesCount = votesCount;
                    if (!voted) {
                        voted = true;
                    }
                }

                PollItemEntry entry = PollItemEntry.byPId(pollItemId);
                entry.votesCount = votesCount;
                entry.voted = voted;
                entry.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        result.putInt(ServiceCallback.PollVoteExtras.VOTES_COUNT, requestedItemVotesCount);
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.PollVoteExtras.ENTRY_POSITION, getRequest().getEntryPosition());
        data.putInt(ServiceCallback.PollVoteExtras.QUESTION_ID, getRequest().getQuestionId());
        data.putInt(ServiceCallback.PollVoteExtras.POLL_ITEM_ID, getRequest().getPollItemId());
        return data;
    }
}

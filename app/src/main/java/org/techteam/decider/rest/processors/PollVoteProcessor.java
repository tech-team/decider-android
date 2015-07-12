package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.PollVoteRequest;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.api.UploadImageRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class PollVoteProcessor extends Processor {
    private static final String TAG = PollVoteProcessor.class.getName();
    private final PollVoteRequest request;

    public PollVoteProcessor(Context context, PollVoteRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.pollVote(request);
            Log.i(TAG, response.toString());

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            int votesCount = response.getInt("votes_count");

            ActiveAndroid.beginTransaction();
            try {
                PollItemEntry entry = PollItemEntry.byPId(request.getPollItemId());
                entry.votesCount = votesCount;
                entry.voted = true;
                entry.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }

            result.putInt(ServiceCallback.PollVoteExtras.VOTES_COUNT, votesCount);
            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException | InvalidAccessTokenException | TokenRefreshFailException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        }

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.PollVoteExtras.QUESTION_ID, request.getQuestionId());
        data.putInt(ServiceCallback.PollVoteExtras.POLL_ITEM_ID, request.getPollItemId());
        return data;
    }
}

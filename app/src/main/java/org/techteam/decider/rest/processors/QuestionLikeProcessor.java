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
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.entityVote(getRequest());
            Log.i(TAG, response.toString());

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            JSONObject data = response.getJSONObject("data");
            int entityId = data.getInt("entity_id");
            int likesCount = data.getInt("likes_count");

            ActiveAndroid.beginTransaction();
            try {
                QuestionEntry entry = QuestionEntry.byQId(entityId);
                entry.likesCount = likesCount;
                entry.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }

            result.putInt(ServiceCallback.EntityVoteExtras.ENTITY_ID, entityId);
            result.putInt(ServiceCallback.PollVoteExtras.VOTES_COUNT, likesCount);
            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException | TokenRefreshFailException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            result.putInt(ServiceCallback.ErrorsExtras.ERROR_CODE, ServiceCallback.ErrorsExtras.Codes.INVALID_TOKEN);
            cb.onError(e.getMessage(), result);
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (ServerErrorException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            result.putInt(ServiceCallback.ErrorsExtras.ERROR_CODE, ServiceCallback.ErrorsExtras.Codes.SERVER_ERROR);
            result.putInt(ServiceCallback.ErrorsExtras.SERVER_ERROR_CODE, e.getCode());
            cb.onError(e.getMessage(), result);
        }

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.EntityVoteExtras.ENTRY_POSITION, getRequest().getEntryPosition());
        return data;
    }
}

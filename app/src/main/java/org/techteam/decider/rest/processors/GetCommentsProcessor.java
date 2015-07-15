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
import org.techteam.decider.rest.api.GetCommentsRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class GetCommentsProcessor extends Processor {
    private static final String TAG = GetCommentsProcessor.class.getName();
    private final GetCommentsRequest request;

    public GetCommentsProcessor(Context context, GetCommentsRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.getComments(request);
            Log.i(TAG, response.toString());

            if (request.getLoadIntention() == LoadIntention.REFRESH) {
                CommentEntry.deleteAll();
            }

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            JSONArray data = response.getJSONArray("data");

            if (data.length() == 0) {
                result.putBoolean(ServiceCallback.GetCommentsExtras.FEED_FINISHED, true);
            } else {

                ActiveAndroid.beginTransaction();
                try {
                    for (int i = 0; i < data.length(); ++i) {
                        JSONObject q = data.getJSONObject(i);
                        CommentEntry entry = CommentEntry.fromJson(q);
                        entry.saveTotal();
                    }
                    ActiveAndroid.setTransactionSuccessful();

                    result.putInt(ServiceCallback.GetCommentsExtras.COUNT, data.length());
                } finally {
                    ActiveAndroid.endTransaction();
                }

                transactionFinished(operationType, requestId);
            }
            cb.onSuccess(result);
        } catch (IOException | JSONException | InvalidAccessTokenException | TokenRefreshFailException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.GetCommentsExtras.LOAD_INTENTION, request.getLoadIntention());
        return data;
    }
}

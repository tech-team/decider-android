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

public class CommentsGetProcessor extends Processor {
    private static final String TAG = CommentsGetProcessor.class.getName();
    private final CommentsGetRequest request;

    public CommentsGetProcessor(Context context, CommentsGetRequest request) {
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

                transactionFinished(operationType, requestId);
            }
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
        data.putInt(ServiceCallback.GetCommentsExtras.LOAD_INTENTION, request.getLoadIntention());
        data.putInt(ServiceCallback.GetCommentsExtras.QUESTION_ID, request.getQuestionId());
        return data;
    }
}

package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.content.entities.UploadedImageEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateCommentRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;

import java.io.IOException;

public class CreateCommentProcessor extends Processor {
    private static final String TAG = CreateCommentProcessor.class.getName();
    private final CreateCommentRequest request;

    public CreateCommentProcessor(Context context, CreateCommentRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.createComment(request);
            Log.i(TAG, response.toString());

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            ActiveAndroid.beginTransaction();
            try {
                UploadedImageEntry.deleteAll();

                JSONObject data = response.getJSONObject("data");
                CommentEntry entry = CommentEntry.fromJson(data);
                entry.saveTotal();
                ActiveAndroid.setTransactionSuccessful();

            } finally {
                ActiveAndroid.endTransaction();
            }

            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException | TokenRefreshFailException | InvalidAccessTokenException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(null, result);
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        return data;
    }
}
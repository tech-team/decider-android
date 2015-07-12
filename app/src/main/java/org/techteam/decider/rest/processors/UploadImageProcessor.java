package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.entities.UploadedImageEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateQuestionRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.api.UploadImageRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class UploadImageProcessor extends Processor {
    private static final String TAG = UploadImageProcessor.class.getName();
    private final UploadImageRequest request;

    public UploadImageProcessor(Context context, UploadImageRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.uploadImage(request);
            Log.i(TAG, response.toString());

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }
            JSONObject data = response.getJSONObject("data");
            String uid = data.getString("uid");

            ActiveAndroid.beginTransaction();
            try {
                UploadedImageEntry entry = new UploadedImageEntry(uid, request.getImageOrdinalId());
                entry.save();
                ActiveAndroid.setTransactionSuccessful();

            } finally {
                ActiveAndroid.endTransaction();
            }


            if (uid == null) {
                transactionError(operationType, requestId);
                cb.onError("Received a null image uid", result);
                return;
            }

            result.putString(ServiceCallback.ImageUploadExtras.UID, uid);

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
        data.putInt(ServiceCallback.ImageUploadExtras.IMAGE_ORDINAL_ID, request.getImageOrdinalId());
        return data;
    }
}

package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateQuestionRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.api.UploadImageRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class UploadImageProcessor extends Processor {
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
            System.out.println(response);

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                transactionError(operationType, requestId);
                cb.onError("status is not ok. status = " + status, result);
                return;
            }

//            ActiveAndroid.beginTransaction();
//            try {
//                QuestionEntry entry = QuestionEntry.fromJson(data);
//                entry.saveTotal();
//                ActiveAndroid.setTransactionSuccessful();

//            } finally {
//                ActiveAndroid.endTransaction();
//            }

            JSONObject data = response.getJSONObject("data");
            String uid = data.getString("uid");
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
        return data;
    }
}

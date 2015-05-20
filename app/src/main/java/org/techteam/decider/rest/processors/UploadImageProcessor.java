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

            if (!response.getString("status").equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                return;
            }

//            ActiveAndroid.beginTransaction();
//            try {
//                JSONObject data = response.getJSONObject("data");
//                QuestionEntry entry = QuestionEntry.fromJson(data);
//                entry.saveTotal();
//                ActiveAndroid.setTransactionSuccessful();
//
//            } finally {
//                ActiveAndroid.endTransaction();
//            }

            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
        } catch (TokenRefreshFailException e) {
            e.printStackTrace();
        }

        transactionError(operationType, requestId);
        cb.onError(null, result);
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        return data;
    }
}

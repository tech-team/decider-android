package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.RegisterRequest;

import java.io.IOException;

public class RegisterProcessor extends Processor {
    private static final String TAG = RegisterProcessor.class.getName();
    private final RegisterRequest request;

    public RegisterProcessor(Context context, RegisterRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.register(request);
            System.out.println(response);


            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            JSONObject data = response.getJSONObject("data");
            JSONObject userJson = data.getJSONObject("user");

            ActiveAndroid.beginTransaction();
            try {
                UserEntry entry = UserEntry.fromJson(userJson, true);
                entry.save();
                ActiveAndroid.setTransactionSuccessful();

                apiUI.setCurrentUserId(entry.getUid());
            } finally {
                ActiveAndroid.endTransaction();
            }


            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        }
    }
}

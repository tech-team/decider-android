package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.RegisterRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class LoginRegisterProcessor extends Processor {
    private static final String TAG = LoginRegisterProcessor.class.getName();
    private final RegisterRequest request;

    public LoginRegisterProcessor(Context context, RegisterRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.loginRegister(request);
            System.out.println(response);


            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. status = " + status, result);
                return;
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

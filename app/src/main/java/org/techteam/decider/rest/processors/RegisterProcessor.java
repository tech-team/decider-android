package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.RegisterRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class RegisterProcessor extends Processor {
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


            if (!response.getString("status").equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                return;
            }

            result.putString(ServiceCallback.RegisterExtras.STATUS, "ok");

            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        transactionError(operationType, requestId);
        cb.onError(null, result);
    }
}

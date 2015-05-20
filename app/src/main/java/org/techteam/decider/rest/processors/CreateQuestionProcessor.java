package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateQuestionRequest;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class CreateQuestionProcessor extends Processor {
    private final CreateQuestionRequest request;

    public CreateQuestionProcessor(Context context, CreateQuestionRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.createQuestion(request);
            System.out.println(response);

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                transactionError(operationType, requestId);
                cb.onError("status is not ok. status = " + status, result);
                return;
            }

            ActiveAndroid.beginTransaction();
            try {
                JSONObject data = response.getJSONObject("data");
                QuestionEntry entry = QuestionEntry.fromJson(data);
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
        }

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        return data;
    }
}

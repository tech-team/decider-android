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
import org.techteam.decider.content.QuestionHelper;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.QuestionsGetRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class QuestionsGetProcessor extends RequestProcessor<QuestionsGetRequest> {
    private static final String TAG = QuestionsGetProcessor.class.getName();

    public QuestionsGetProcessor(Context context, QuestionsGetRequest request) {
        super(context, request);
    }


    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.getQuestions(getRequest());
            Log.i(TAG, response.toString());

            if (getRequest().getLoadIntention() == LoadIntention.REFRESH) {
                QuestionHelper.deleteAll(getRequest().getContentSection());
            }

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }

            JSONArray data = response.getJSONArray("data");
            if (data.length() == 0) {
                result.putInt(ServiceCallback.GetQuestionsExtras.COUNT, 0);
                result.putBoolean(ServiceCallback.GetQuestionsExtras.FEED_FINISHED, true);
            } else {
                ActiveAndroid.beginTransaction();
                try {
                    for (int i = 0; i < data.length(); ++i) {
                        JSONObject q = data.getJSONObject(i);
                        QuestionEntry question = QuestionEntry.fromJson(q);

                        QuestionHelper.saveQuestion(getRequest().getContentSection(), question);
                    }
                    ActiveAndroid.setTransactionSuccessful();

                    result.putInt(ServiceCallback.GetQuestionsExtras.COUNT, data.length());
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
        data.putInt(ServiceCallback.GetQuestionsExtras.LOAD_INTENTION, getRequest().getLoadIntention());
        data.putInt(ServiceCallback.GetQuestionsExtras.SECTION, getRequest().getContentSection().toInt());
        return data;
    }
}

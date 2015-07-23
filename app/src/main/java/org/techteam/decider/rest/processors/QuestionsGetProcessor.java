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
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.getQuestions(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        if (getRequest().getLoadIntention() == LoadIntention.REFRESH) {
            QuestionHelper.deleteAll(getRequest().getContentSection());
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

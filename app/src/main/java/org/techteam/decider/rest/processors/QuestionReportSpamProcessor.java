package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.QuestionLikeRequest;
import org.techteam.decider.rest.api.QuestionReportSpamRequest;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class QuestionReportSpamProcessor extends RequestProcessor<QuestionReportSpamRequest> {
    private static final String TAG = QuestionReportSpamProcessor.class.getName();

    public QuestionReportSpamProcessor(Context context, QuestionReportSpamRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.reportSpam(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {

    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putString(ServiceCallback.ReportSpamExtras.ENTITY_NAME, getRequest().getEntityType());
        data.putInt(ServiceCallback.ReportSpamExtras.ENTITY_ID, getRequest().getEntityId());
        data.putInt(ServiceCallback.ReportSpamExtras.ENTRY_POSITION, getRequest().getEntryPosition());
        return data;
    }
}

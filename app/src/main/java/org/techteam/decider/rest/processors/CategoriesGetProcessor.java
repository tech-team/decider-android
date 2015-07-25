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
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CategoriesGetRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class CategoriesGetProcessor extends RequestProcessor<CategoriesGetRequest> {
    private static final String TAG = CategoriesGetProcessor.class.getName();

    public CategoriesGetProcessor(Context context, CategoriesGetRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.getCategories(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        ActiveAndroid.beginTransaction();
        try {
            JSONArray data = response.getJSONArray("data");
            for (int i = 0; i < data.length(); ++i) {
                JSONObject q = data.getJSONObject(i);
                CategoryEntry entry = CategoryEntry.fromJson(q);
                CategoryEntry dbEntry = CategoryEntry.byUid(entry.getUid());
                if (dbEntry == null || !dbEntry.contentEquals(entry)) {
                    entry.setSelected(true);
                    entry.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();

            result.putInt(ServiceCallback.GetCategoriesExtras.COUNT, data.length());
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
}

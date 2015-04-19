package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.GetCategoriesRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class GetCategoriesProcessor extends Processor {
    private final GetCategoriesRequest request;

    public GetCategoriesProcessor(Context context, GetCategoriesRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.getCategories(request);
            System.out.println(response);

            if (response == null) {
                cb.onError("No categories found", result);
                return;
            }

            if (!response.getString("status").equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                return;
            }

            ActiveAndroid.beginTransaction();
            try {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); ++i) {
                    JSONObject q = data.getJSONObject(i);
                    CategoryEntry entry = CategoryEntry.fromJson(q);
                    CategoryEntry dbEntry = CategoryEntry.byUid(entry.getUid());
                    if (dbEntry == null || !dbEntry.contentEquals(entry)) {
                        entry.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();

                result.putInt(ServiceCallback.GetCategoriesExtras.COUNT, data.length());
            } finally {
                ActiveAndroid.endTransaction();
            }

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
}

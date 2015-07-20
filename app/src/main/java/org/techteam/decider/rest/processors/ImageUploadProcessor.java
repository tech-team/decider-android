package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.UploadedImageEntry;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.api.ImageUploadRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class ImageUploadProcessor extends RequestProcessor<ImageUploadRequest> {
    private static final String TAG = ImageUploadProcessor.class.getName();

    public ImageUploadProcessor(Context context, ImageUploadRequest request) {
        super(context, request);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException {
        return apiUI.uploadImage(getRequest());
    }

    @Override
    public void postExecute(JSONObject response, Bundle result) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        String uid = data.getString("uid");

        ActiveAndroid.beginTransaction();
        try {
            UploadedImageEntry entry = new UploadedImageEntry(uid, getRequest().getImageOrdinalId());
            entry.save();
            ActiveAndroid.setTransactionSuccessful();

        } finally {
            ActiveAndroid.endTransaction();
        }

        result.putString(ServiceCallback.ImageUploadExtras.UID, uid);
    }

    @Override
    protected Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.ImageUploadExtras.IMAGE_ORDINAL_ID, getRequest().getImageOrdinalId());
        return data;
    }
}

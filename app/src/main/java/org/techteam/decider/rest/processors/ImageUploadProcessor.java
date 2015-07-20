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
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.uploadImage(getRequest());
            Log.i(TAG, response.toString());

            String status = response.getString("status");
            if (!status.equalsIgnoreCase("ok")) {
                transactionError(operationType, requestId);
                cb.onError("status is not ok. resp = " + response.toString(), result);
                return;
            }
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


            if (uid == null) {
                transactionError(operationType, requestId);
                cb.onError("Received a null image uid", result);
                return;
            }

            result.putString(ServiceCallback.ImageUploadExtras.UID, uid);

            transactionFinished(operationType, requestId);
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
        data.putInt(ServiceCallback.ImageUploadExtras.IMAGE_ORDINAL_ID, getRequest().getImageOrdinalId());
        return data;
    }
}

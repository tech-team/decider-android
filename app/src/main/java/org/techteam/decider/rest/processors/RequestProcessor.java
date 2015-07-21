package org.techteam.decider.rest.processors;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.ServerErrorException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public abstract class RequestProcessor<T> extends Processor {

    private final T request;

    public RequestProcessor(Context context, T request) {
        super(context);
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    protected boolean checkResponse(JSONObject response, OperationType operationType, String requestId, Bundle result, ProcessorCallback cb) throws JSONException {
        if (response == null) {
            transactionError(operationType, requestId);
            cb.onError("Received no response", result);
            return false;
        }
        String status = response.getString("status");
        if (!status.equalsIgnoreCase("ok")) {
            int code = response.getInt("code");
            postExecuteError(response, code, result);
            transactionError(operationType, requestId);
            cb.onError("status is not ok. resp = " + response.toString(), result);
            return false;
        }
        return true;
    }

    public abstract JSONObject executeRequest() throws ServerErrorException, OperationCanceledException, TokenRefreshFailException, IOException, JSONException, InvalidAccessTokenException, AuthenticatorException;
    public abstract void postExecute(JSONObject response, Bundle result) throws JSONException;
    public void postExecuteError(JSONObject response, int errorCode, Bundle result) throws JSONException {
        result.putInt(ServiceCallback.ErrorsExtras.SERVER_ERROR_CODE, errorCode);
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {
        transactionStarted(operationType, requestId);
        Bundle result = getInitialBundle();
        try {
            JSONObject response = executeRequest();
            Log.i(getTag(), response.toString());

            if (!checkResponse(response, operationType, requestId, result, cb)) {
                return;
            }

            postExecute(response, result);
            transactionFinished(operationType, requestId);
            cb.onSuccess(result);
        } catch (IOException | JSONException | TokenRefreshFailException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            cb.onError(e.getMessage(), result);
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            result.putInt(ServiceCallback.ErrorsExtras.GENERIC_ERROR_CODE, ServiceCallback.ErrorsExtras.GenericErrors.INVALID_TOKEN);
            cb.onError(e.getMessage(), result);
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (ServerErrorException e) {
            e.printStackTrace();
            transactionError(operationType, requestId);
            result.putInt(ServiceCallback.ErrorsExtras.GENERIC_ERROR_CODE, ServiceCallback.ErrorsExtras.GenericErrors.SERVER_ERROR);
            result.putInt(ServiceCallback.ErrorsExtras.INTERNAL_SERVER_ERROR, e.getCode());
            cb.onError(e.getMessage(), result);
        }
    }

    protected abstract String getTag();
}

package org.techteam.decider.rest.service_helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.PendingOperation;
import org.techteam.decider.rest.api.UploadImageRequest;
import org.techteam.decider.rest.service.DeciderService;
import org.techteam.decider.rest.service.ServiceIntentBuilder;
import org.techteam.decider.util.CallbackHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceHelper {
    private static final String TAG = ServiceHelper.class.getName();
    private final Context context;
    private CallbackHelper<String, ServiceCallback> callbackHelper = new CallbackHelper<String, ServiceCallback>();
    private Map<String, PendingOperation> pendingOperations = new HashMap<>();
    private boolean isInit = false;
    private ServiceBroadcastReceiver receiver;

    public ServiceHelper(Context context) {
        this.context = context;
    }

    public void getQuestions(ContentSection contentSection, int limit, int offset, Collection<CategoryEntry> categories, int loadIntention, ServiceCallback cb) {
        init();

        OperationType op = OperationType.GET_QUESTIONS;

        String requestId = createRequestId(op, contentSection, limit, offset);
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.getQuestionsIntent(context, op, requestId, contentSection, limit, offset, categories, loadIntention);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void getCategories(String locale, ServiceCallback cb) {
        init();

        OperationType op = OperationType.GET_CATEGORIES;

        String requestId = createRequestId(op, locale);
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.getCategoriesIntent(context, op, requestId, locale);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void loginRegister(String email, String password, ServiceCallback cb) {
        init();
        email = email.trim();

        OperationType op = OperationType.LOGIN_REGISTER;

        String requestId = createRequestId(op, email);
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.registerIntent(context, op, requestId, email, password);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void uploadImage(UploadImageRequest.Image image, int imageOrdinalId, ServiceCallback cb) {
        init();

        OperationType op = OperationType.UPLOAD_IMAGE;

        String requestId = createRequestId(op, imageOrdinalId, image.getOriginalFilename());
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.uploadImageIntent(context, op, requestId, image, imageOrdinalId);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void createQuestion(QuestionData questionData, ServiceCallback cb) {
        init();

        OperationType op = OperationType.CREATE_QUESTION;

        String requestId = createRequestId(op, questionData.createFingerprint());
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.createQuestionIntent(context, op, requestId, questionData);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void pollVote(int questionId, int pollItemId, ServiceCallback cb) {
        init();

        OperationType op = OperationType.POLL_VOTE;

        String requestId = createRequestId(op, questionId, pollItemId);
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.pollVoteIntent(context, op, requestId, questionId, pollItemId);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void getComments(int questionId, int limit, int offset, int loadIntention, ServiceCallback cb) {
        init();

        OperationType op = OperationType.GET_COMMENTS;

        String requestId = createRequestId(op, questionId, limit, offset);
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.getCommentsIntent(context, op, requestId, questionId, limit, offset, loadIntention);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(op, requestId));
    }

    public void saveOperationsState(Bundle outState, String key) {
        outState.putParcelableArrayList(key, new ArrayList<>(pendingOperations.values()));
    }

    /**
     * @return true if app should be refreshing right now, false otherwise
     **/
    public boolean restoreOperationsState(Bundle savedInstanceState, String key, CallbacksKeeper callbacksKeeper) {
        ArrayList<PendingOperation> operations = savedInstanceState.getParcelableArrayList(key);
        if (operations == null) {
            Log.wtf(TAG, "Operations are null!");
            return false;
        }
        for (PendingOperation op : operations) {
            pendingOperations.put(op.getOperationId(), op);
        }

        boolean isRefreshing = false;

        // callbacks are subscribed again to restored pending operations
        // TODO: seems like incorrect
        for (String opId : pendingOperations.keySet()) {
            PendingOperation op = pendingOperations.get(opId);
            if (!isRefreshing) {
                isRefreshing = op.getOperationType().canRefresh();
            }

            addCallback(op.getOperationId(), callbacksKeeper.getCallback(op.getOperationType()));
        }
        return isRefreshing;
    }

    public void init() {
        if (!isInit) {
            IntentFilter filter = new IntentFilter(ServiceBroadcastReceiverHelper.NAME);
            receiver = new ServiceBroadcastReceiver();
            LocalBroadcastManager.getInstance(context)
                    .registerReceiver(receiver, filter);
            isInit = true;
        }
    }

    public void release() {
        if (isInit) {
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(receiver);
            isInit = false;
        }
    }

    public void addCallback(String operationId, ServiceCallback cb) {
        callbackHelper.addCallback(operationId, cb);
    }

    private static String createRequestId(OperationType op, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(op.toString());
        for (Object arg : args) {
            sb.append("__");
            sb.append(arg.toString().replace("__", ""));
        }
        return sb.toString();
    }



    public class ServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String id = extras.getString(DeciderService.CallbackIntentExtras.REQUEST_ID);
            OperationType operation = Enum.valueOf(OperationType.class, extras.getString(DeciderService.CallbackIntentExtras.OPERATION));

            int status = extras.getInt(DeciderService.CallbackIntentExtras.STATUS);
            String errorMsg = extras.getString(DeciderService.CallbackIntentExtras.ERROR_MSG);
            Bundle data = extras.getBundle(DeciderService.CallbackIntentExtras.EXTRA_DATA);

            List<ServiceCallback> callbacks = callbackHelper.getCallbacks(id);
            if (callbacks != null) {
                for (ServiceCallback cb : callbacks) {
                    if (status == DeciderService.CallbackIntentExtras.Status.OK) {
                        cb.onSuccess(id, data);
                    } else {
                        cb.onError(id, data, errorMsg);
                    }
                    pendingOperations.remove(id);
                }
            }

            callbackHelper.removeCallbacks(id);
        }
    }

    public static class ServiceBroadcastReceiverHelper {
        public static final String NAME = ServiceBroadcastReceiver.class.getName();
    }
}

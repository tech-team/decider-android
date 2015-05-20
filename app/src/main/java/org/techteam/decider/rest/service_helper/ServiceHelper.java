package org.techteam.decider.rest.service_helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.PendingOperation;
import org.techteam.decider.rest.service.DeciderService;
import org.techteam.decider.rest.service.ServiceIntentBuilder;
import org.techteam.decider.util.CallbackHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceHelper {
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

        String requestId = OperationType.GET_QUESTIONS + "__" + limit + "__" + offset;
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.getQuestionsIntent(context, requestId, contentSection, limit, offset, categories, loadIntention);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(OperationType.GET_QUESTIONS, requestId));
    }

    public void getCategories(String locale, ServiceCallback cb) {
        init();

        String requestId = OperationType.GET_CATEGORIES + "__" + locale;
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.getCategoriesIntent(context, requestId, locale);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(OperationType.GET_CATEGORIES, requestId));
    }

    public void register(String email, String password, ServiceCallback cb) {
        init();

        String requestId = OperationType.REGISTER + "__" + email;
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.registerIntent(context, requestId, email, password);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(OperationType.REGISTER, requestId));
    }

    public void uploadImage(Bitmap image, ServiceCallback cb) {
        init();

        String requestId = OperationType.UPLOAD_IMAGE + "__" + "123123123123123123123123"; // TODO
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.uploadImageIntent(context, requestId, image);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(OperationType.UPLOAD_IMAGE, requestId));
    }

    public void createQuestion(QuestionData questionData, ServiceCallback cb) {
        init();

        String requestId = OperationType.CREATE_QUESTION + "__" + questionData.createFingerprint();
        CallbackHelper.AddStatus s = callbackHelper.addCallback(requestId, cb);

        if (s == CallbackHelper.AddStatus.NEW_CB) {
            Intent intent = ServiceIntentBuilder.createQuestionIntent(context, requestId, questionData);
            context.startService(intent);
        }

        pendingOperations.put(requestId, new PendingOperation(OperationType.CREATE_QUESTION, requestId));
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
            System.err.println("Operations are null!!!");
            return false;
        }
        for (PendingOperation op : operations) {
            pendingOperations.put(op.getOperationId(), op);
        }

        boolean isRefreshing = false;

        // callbacks are subscribed again to restored pending operations
        for (String opId : pendingOperations.keySet()) {
            PendingOperation op = pendingOperations.get(opId);
            if (op.getOperationType() == OperationType.GET_QUESTIONS)
                isRefreshing = true;

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

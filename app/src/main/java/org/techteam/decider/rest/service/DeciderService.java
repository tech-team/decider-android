package org.techteam.decider.rest.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.GetCategoriesRequest;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.api.RegisterRequest;
import org.techteam.decider.rest.processors.GetCategoriesProcessor;
import org.techteam.decider.rest.processors.GetQuestionsProcessor;
import org.techteam.decider.rest.processors.Processor;
import org.techteam.decider.rest.processors.ProcessorCallback;
import org.techteam.decider.rest.processors.RegisterProcessor;
import org.techteam.decider.rest.service_helper.ServiceHelper;


public class DeciderService extends IntentService {
    public static final String TAG = DeciderService.class.getName();

    public DeciderService() {
        super(TAG);
    }

    public DeciderService(String name) {
        super(name);
    }

    public class IntentExtras {
        public static final String REQUEST_ID = "REQUEST_ID";
        public static final String OPERATION = "OPERATION";
    }

    public class CallbackIntentExtras {
        public static final String REQUEST_ID = "REQUEST_ID";
        public static final String OPERATION = "OPERATION";
        public static final String STATUS = "STATUS";
        public static final String ERROR_MSG = "ERROR_MSG";
        public static final String EXTRA_DATA = "EXTRA_DATA";

        public class Status {
            public static final int OK = 0;
            public static final int ERROR = 1;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("=== Started DeciderService ===");
        Bundle extras = intent.getExtras();
        String requestId = extras.getString(IntentExtras.REQUEST_ID);
        OperationType operation = null;
        try {
            operation = Enum.valueOf(OperationType.class, extras.getString(IntentExtras.OPERATION));
        } catch (IllegalArgumentException ignored) {
        }

        if (requestId == null || operation == null) {
            throw new RuntimeException("No REQUEST_ID or operation specified");
        }

        Processor processor = null;

        switch (operation) {

            case GET_QUESTIONS: {
                GetQuestionsRequest request = GetQuestionsRequest.fromBundle(extras);
                processor = new GetQuestionsProcessor(getBaseContext(), request);
                break;
            }
            case GET_CATEGORIES: {
                GetCategoriesRequest request = GetCategoriesRequest.fromBundle(extras);
                processor = new GetCategoriesProcessor(getBaseContext(), request);
                break;
            }
            case REGISTER: {
                RegisterRequest request = RegisterRequest.fromBundle(extras);
                processor = new RegisterProcessor(getBaseContext(), request);
                break;
            }
            case LOGIN:
                break;
        }

//        } else if (operation == OperationType.BASH_VOTE) {
//
//            int entryPosition = intent.getIntExtra(IntentExtras.BashVoteOperation.ENTRY_POSITION, -1);
//            String entryId = extras.getString(IntentExtras.BashVoteOperation.ENTRY_ID);
//            String rating = extras.getString(IntentExtras.BashVoteOperation.RATING);
//            int direction = extras.getInt(IntentExtras.BashVoteOperation.DIRECTION, 100); // 100 is a value to break
////            boolean bayaning = extras.getBoolean(IntentExtras.BashVoteOperation.BAYAN, false);
//
//            processor = new BashVoteProcessor(getBaseContext(), entryPosition, entryId, rating, direction);
//        } else if (operation == OperationType.IT_VOTE) {
//            // TODO
//
//            processor = null;
//        }

        final Intent cbIntent = new Intent(ServiceHelper.ServiceBroadcastReceiverHelper.NAME);
        cbIntent.putExtra(CallbackIntentExtras.REQUEST_ID, requestId);
        cbIntent.putExtra(CallbackIntentExtras.OPERATION, operation.toString());

        if (processor != null) {
            processor.start(operation, requestId, new ProcessorCallback() {
                @Override
                public void onSuccess(Bundle data) {
                    cbIntent.putExtra(CallbackIntentExtras.STATUS, CallbackIntentExtras.Status.OK);
                    cbIntent.putExtra(CallbackIntentExtras.EXTRA_DATA, data);
                    LocalBroadcastManager.getInstance(DeciderService.this).sendBroadcast(cbIntent);
                }

                @Override
                public void onError(String message, Bundle data) {
                    cbIntent.putExtra(CallbackIntentExtras.STATUS, CallbackIntentExtras.Status.ERROR);
                    cbIntent.putExtra(CallbackIntentExtras.ERROR_MSG, message);
                    cbIntent.putExtra(CallbackIntentExtras.EXTRA_DATA, data);
                    LocalBroadcastManager.getInstance(DeciderService.this).sendBroadcast(cbIntent);
                }
            });

        } else {
            cbIntent.putExtra(CallbackIntentExtras.STATUS, CallbackIntentExtras.Status.ERROR);
            cbIntent.putExtra(CallbackIntentExtras.ERROR_MSG, "Processor not found");
            LocalBroadcastManager.getInstance(DeciderService.this).sendBroadcast(cbIntent);
        }
    }


}

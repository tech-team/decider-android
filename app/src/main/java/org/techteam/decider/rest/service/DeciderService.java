package org.techteam.decider.rest.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CommentCreateRequest;
import org.techteam.decider.rest.api.QuestionCreateRequest;
import org.techteam.decider.rest.api.CategoriesGetRequest;
import org.techteam.decider.rest.api.CommentsGetRequest;
import org.techteam.decider.rest.api.QuestionsGetRequest;
import org.techteam.decider.rest.api.UserEditRequest;
import org.techteam.decider.rest.api.UserGetRequest;
import org.techteam.decider.rest.api.LoginRegisterRequest;
import org.techteam.decider.rest.api.PollVoteRequest;
import org.techteam.decider.rest.api.ImageUploadRequest;
import org.techteam.decider.rest.processors.CommentCreateProcessor;
import org.techteam.decider.rest.processors.QuestionCreateProcessor;
import org.techteam.decider.rest.processors.CategoriesGetProcessor;
import org.techteam.decider.rest.processors.CommentsGetProcessor;
import org.techteam.decider.rest.processors.QuestionsGetProcessor;
import org.techteam.decider.rest.processors.UserEditProcessor;
import org.techteam.decider.rest.processors.UserGetProcessor;
import org.techteam.decider.rest.processors.LoginRegisterProcessor;
import org.techteam.decider.rest.processors.PollVoteProcessor;
import org.techteam.decider.rest.processors.Processor;
import org.techteam.decider.rest.processors.ProcessorCallback;
import org.techteam.decider.rest.processors.ImageUploadProcessor;
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

            case QUESTIONS_GET: {
                QuestionsGetRequest request = QuestionsGetRequest.fromBundle(extras);
                processor = new QuestionsGetProcessor(getBaseContext(), request);
                break;
            }
            case CATEGORIES_GET: {
                CategoriesGetRequest request = CategoriesGetRequest.fromBundle(extras);
                processor = new CategoriesGetProcessor(getBaseContext(), request);
                break;
            }
            case LOGIN:
            case REGISTER: {
                LoginRegisterRequest request = LoginRegisterRequest.fromBundle(extras);
                processor = new LoginRegisterProcessor(getBaseContext(), request);
                break;
            }
            case QUESTION_CREATE: {
                QuestionCreateRequest request = QuestionCreateRequest.fromBundle(extras);
                processor = new QuestionCreateProcessor(getBaseContext(), request);
                break;
            }
            case IMAGE_UPLOAD: {
                ImageUploadRequest request = ImageUploadRequest.fromBundle(extras);
                processor = new ImageUploadProcessor(getBaseContext(), request);
                break;
            }
            case POLL_VOTE: {
                PollVoteRequest request = PollVoteRequest.fromBundle(extras);
                processor = new PollVoteProcessor(getBaseContext(), request);
                break;
            }
            case COMMENTS_GET: {
                CommentsGetRequest request = CommentsGetRequest.fromBundle(extras);
                processor = new CommentsGetProcessor(getBaseContext(), request);
                break;
            }
            case COMMENT_CREATE: {
                CommentCreateRequest request = CommentCreateRequest.fromBundle(extras);
                processor = new CommentCreateProcessor(getBaseContext(), request);
                break;
            }
            case USER_GET: {
                UserGetRequest request = UserGetRequest.fromBundle(extras);
                processor = new UserGetProcessor(getBaseContext(), request);
                break;
            }
            case USER_EDIT: {
                UserEditRequest request = UserEditRequest.fromBundle(extras);
                processor = new UserEditProcessor(getBaseContext(), request);
                break;
            }

        }

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

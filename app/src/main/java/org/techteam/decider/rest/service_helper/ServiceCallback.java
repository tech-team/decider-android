package org.techteam.decider.rest.service_helper;

import android.os.Bundle;

public interface ServiceCallback {
    void onSuccess(String operationId, Bundle data);
    void onError(String operationId, Bundle data, String message);

    class ErrorsExtras {
        public static final String ERROR_CODE = "ERROR_CODE";

        public class Codes {
            public static final int INVALID_TOKEN = 1;
        }
    }

    class GetQuestionsExtras {
        public static final String FEED_FINISHED = "FEED_FINISHED";
        public static final String COUNT = "COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
        public static final String SECTION = "SECTION";
    }

    class GetCategoriesExtras {
        public static final String COUNT = "COUNT";
    }

    class ImageUploadExtras {
        public static final String UID = "UID";
        public static final String IMAGE_ORDINAL_ID = "IMAGE_ORDINAL_ID";
    }

    class PollVoteExtras {
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String POLL_ITEM_ID = "POLL_ITEM_ID";
        public static final String VOTES_COUNT = "VOTES_COUNT";
    }

    class GetCommentsExtras {
        public static final String FEED_FINISHED = "FEED_FINISHED";
        public static final String COUNT = "COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    class CreateQuestionExtras {
        public static final String QID = "QID";
    }

    class LoginRegisterExtras {
        public static final String LOGIN = "LOGIN";
        public static final String PASSWORD = "PASSWORD";
        public static final String TOKEN = "TOKEN";
        public static final String EXPIRES = "EXPIRES";
        public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
        public static final String USER_ID = "USER_ID";
    }
}

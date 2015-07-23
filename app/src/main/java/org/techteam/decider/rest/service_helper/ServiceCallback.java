package org.techteam.decider.rest.service_helper;

import android.os.Bundle;

public interface ServiceCallback {
    void onSuccess(String operationId, Bundle data);
    void onError(String operationId, Bundle data, String message);

    class ErrorsExtras {
        public static final String GENERIC_ERROR_CODE = "GENERIC_ERROR_CODE";
        public static final String SERVER_ERROR_CODE = "SERVER_ERROR_CODE";
        public static final String SERVER_ERROR_MSG = "SERVER_ERROR_MSG";
        public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

        public class GenericErrors {
            public static final int INVALID_TOKEN = 1;
            public static final int SERVER_ERROR = 2;
            public static final int NO_INTERNET = 3;
            public static final int INTERNAL_PROBLEMS = 4;
        }

        public class ErrorCodes {
            public static final int REGISTRATION_UNFINISHED = 3000;
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
        public static final String ENTRY_POSITION = "ENTRY_POSITION";

        public class ErrorCodes {
            public static final int ALREADY_VOTED = 8200;
        }
    }

    class EntityVoteExtras {
        public static final String ENTITY_NAME = "ENTITY_NAME";
        public static final String ENTITY_ID = "ENTITY_ID";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String VOTES_COUNT = "VOTES_COUNT";
    }

    class GetCommentsExtras {
        public static final String FEED_FINISHED = "FEED_FINISHED";
        public static final String COUNT = "COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String REMAINING = "REMAINING";
    }

    class CreateQuestionExtras {
        public static final String QID = "QID";
        public static final String COUNT = "COUNT";
    }

    class LoginRegisterExtras {
        public static final String LOGIN = "LOGIN";
        public static final String PASSWORD = "PASSWORD";
        public static final String TOKEN = "TOKEN";
        public static final String EXPIRES = "EXPIRES";
        public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
        public static final String USER_ID = "USER_ID";
        public static final String USERNAME = "USERNAME";
        public static final String REGISTRATION_UNFINISHED = "REGISTRATION_UNFINISHED";

        public class ErrorCodes {
            public static final int INVALID_CREDENTIALS = 7002;
        }
    }

    class GetUserExtras {
        public static final String USER_ID = "USER_ID";
    }

    class EditUserExtras {
        public static final String USER_ID = "USER_ID";
        public static final String USERNAME = "USERNAME";

        public class ErrorCodes {
            public static final int USERNAME_TAKEN = 7006;
            public static final int USERNAME_REQUIRED = 7003;
        }
    }

    class ReportSpamExtras {
        public static final String ENTITY_NAME = "ENTITY_NAME";
        public static final String ENTITY_ID = "ENTITY_ID";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
    }
}

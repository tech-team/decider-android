package org.techteam.decider.rest.service_helper;

import android.os.Bundle;

public interface ServiceCallback {
    void onSuccess(String operationId, Bundle data);
    void onError(String operationId, Bundle data, String message);

    class GetQuestionsExtras {
        public static final String FEED_FINISHED = "FEED_FINISHED";
        public static final String COUNT = "COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    class GetCategoriesExtras {
        public static final String COUNT = "COUNT";
    }

    class RegisterExtras {
        public static final String STATUS = "STATUS";
    }

//    class BashVoteExtras {
//        public static final String ENTRY_ID = "ENTRY_ID";
//        public static final String ENTRY_POSITION = "ENTRY_POSITION";
//    }
}

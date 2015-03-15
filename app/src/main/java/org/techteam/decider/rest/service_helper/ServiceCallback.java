package org.techteam.decider.rest.service_helper;

import android.os.Bundle;

public interface ServiceCallback {
    void onSuccess(String operationId, Bundle data);
    void onError(String operationId, Bundle data, String message);

    class GetPostsExtras {
        public static final String NEW_CONTENT_SOURCE = "NEW_CONTENT_SOURCE";
        public static final String FEED_FINISHED = "FEED_FINISHED";
        public static final String INSERTED_COUNT = "INSERTED_COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    class BashVoteExtras {
        public static final String ENTRY_ID = "ENTRY_ID";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
    }
}

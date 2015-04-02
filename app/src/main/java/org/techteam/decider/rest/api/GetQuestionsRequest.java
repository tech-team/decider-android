package org.techteam.decider.rest.api;

import android.content.Context;
import android.os.Bundle;

import org.techteam.decider.content.ContentSection;

public class GetQuestionsRequest {
    private final ContentSection contentSection;
    private final int limit;
    private final int offset;
    private final int loadIntention;

    public static final String URL = "/questions";

    public class IntentExtras {
        public static final String CONTENT_SECTION = "CONTENT_SECTION";
        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    public GetQuestionsRequest(ContentSection contentSection, int limit, int offset, int loadIntention) {
        this.contentSection = contentSection;
        this.limit = limit;
        this.offset = offset;
        this.loadIntention = loadIntention;
    }

    public static GetQuestionsRequest fromBundle(Bundle bundle) {
        ContentSection contentSection = ContentSection.fromInt(bundle.getInt(IntentExtras.CONTENT_SECTION, -1));
        int limit = bundle.getInt(IntentExtras.LIMIT);
        int offset = bundle.getInt(IntentExtras.OFFSET);
        int loadIntention = bundle.getInt(IntentExtras.LOAD_INTENTION);

        return new GetQuestionsRequest(contentSection, limit, offset, loadIntention);
    }

    public ContentSection getContentSection() {
        return contentSection;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLoadIntention() {
        return loadIntention;
    }
}

package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.ContentSection;

public class QuestionsGetRequest extends ApiRequest {
    private final ContentSection contentSection;
    private final int firstQuestionId;
    private final int limit;
    private final int offset;
    private final int loadIntention;
    private final int[] categories;

    public static final String URL = "questions";

    public class IntentExtras {
        public static final String CONTENT_SECTION = "CONTENT_SECTION";
        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
        public static final String CATEGORIES = "CATEGORIES";
        public static final String FIRST_QUESTION_ID = "FIRST_QUESTION_ID";
    }

    public QuestionsGetRequest(ContentSection contentSection, int firstQuestionId, int limit, int offset, int[] categories, int loadIntention) {
        this.firstQuestionId = firstQuestionId;
        this.contentSection = contentSection;
        this.limit = limit;
        this.offset = offset;
        this.categories = categories;
        this.loadIntention = loadIntention;
    }

    public static QuestionsGetRequest fromBundle(Bundle bundle) {
        ContentSection contentSection = ContentSection.fromInt(bundle.getInt(IntentExtras.CONTENT_SECTION, -1));
        System.out.println("ContentSection = " + contentSection.toString());
        int firstQuestionId = bundle.getInt(IntentExtras.FIRST_QUESTION_ID);
        int limit = bundle.getInt(IntentExtras.LIMIT);
        int offset = bundle.getInt(IntentExtras.OFFSET);
        int loadIntention = bundle.getInt(IntentExtras.LOAD_INTENTION);
        int[] categories = bundle.getIntArray(IntentExtras.CATEGORIES);

        return new QuestionsGetRequest(contentSection, firstQuestionId, limit, offset, categories, loadIntention);
    }

    public ContentSection getContentSection() {
        return contentSection;
    }

    public int getFirstQuestionId() {
        return firstQuestionId;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int[] getCategories() {
        return categories;
    }

    public int getLoadIntention() {
        return loadIntention;
    }

    @Override
    public String getPath() {
        return URL;
    }
}

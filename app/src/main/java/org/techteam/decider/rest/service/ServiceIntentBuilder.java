package org.techteam.decider.rest.service;

import android.content.Context;
import android.content.Intent;

import org.techteam.decider.content.entities.ContentCategory;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.GetCategoriesRequest;
import org.techteam.decider.rest.api.GetQuestionsRequest;

import java.util.Collection;

public final class ServiceIntentBuilder {

    public static Intent getBasicIntent(Context context, String requestId, OperationType operationType) {
        Intent intent = new Intent(context, DeciderService.class);
        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
        intent.putExtra(DeciderService.IntentExtras.OPERATION, operationType.toString());
        return intent;
    }

    public static Intent getQuestionsIntent(Context context, String requestId, ContentSection contentSection, int limit, int offset, Collection<ContentCategory> categories, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, OperationType.GET_QUESTIONS);

        intent.putExtra(GetQuestionsRequest.IntentExtras.CONTENT_SECTION, contentSection.toInt());
        intent.putExtra(GetQuestionsRequest.IntentExtras.LIMIT, limit);
        intent.putExtra(GetQuestionsRequest.IntentExtras.OFFSET, offset);

        int[] categories_ids = new int[categories.size()];
        int i = 0;
        for (ContentCategory c : categories) {
            categories_ids[i++] = c.getUid();
        }
        intent.putExtra(GetQuestionsRequest.IntentExtras.CATEGORIES, categories_ids);
        intent.putExtra(GetQuestionsRequest.IntentExtras.LOAD_INTENTION, loadIntention);
        return intent;
    }

    public static Intent getCategoriesIntent(Context context, String requestId, String locale) {
        Intent intent = getBasicIntent(context, requestId, OperationType.GET_CATEGORIES);
        intent.putExtra(GetCategoriesRequest.IntentExtras.LOCALE, locale);
        return intent;
    }
//
//    public static Intent voteBashIntent(Context context, String requestId, int entryPosition, String entryId, String rating, int direction) {
//        Intent intent = new Intent(context, DeciderService.class);
//        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
//        intent.putExtra(DeciderService.IntentExtras.OPERATION, OperationType.BASH_VOTE.toString());
//
//        intent.putExtra(DeciderService.IntentExtras.BashVoteOperation.ENTRY_POSITION, entryPosition);
//        intent.putExtra(DeciderService.IntentExtras.BashVoteOperation.ENTRY_ID, entryId);
//        intent.putExtra(DeciderService.IntentExtras.BashVoteOperation.RATING, rating);
//        intent.putExtra(DeciderService.IntentExtras.BashVoteOperation.DIRECTION, direction);
//        return intent;
//    }

}
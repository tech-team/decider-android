package org.techteam.decider.rest.service;

import android.content.Context;
import android.content.Intent;

import org.techteam.decider.content.ContentSection;
import org.techteam.decider.rest.OperationType;

public final class ServiceIntentBuilder {

    public static Intent getQuestionsIntent(Context context, String requestId, ContentSection contentSection, int limit, int offset, int loadIntention) {
        Intent intent = new Intent(context, DeciderService.class);
        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
        intent.putExtra(DeciderService.IntentExtras.OPERATION, OperationType.GET_QUESETIONS.toString());

        intent.putExtra(DeciderService.IntentExtras.GetQuestionsOperation.CONTENT_SECTION, contentSection.toInt());
        intent.putExtra(DeciderService.IntentExtras.GetQuestionsOperation.LIMIT, limit);
        intent.putExtra(DeciderService.IntentExtras.GetQuestionsOperation.OFFSET, offset);
        intent.putExtra(DeciderService.IntentExtras.GetQuestionsOperation.LOAD_INTENTION, loadIntention);
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
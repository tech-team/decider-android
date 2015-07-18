package org.techteam.decider.rest.service;

import android.content.Context;
import android.content.Intent;

import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.content.question.ImageData;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateCommentRequest;
import org.techteam.decider.rest.api.CreateQuestionRequest;
import org.techteam.decider.rest.api.GetCategoriesRequest;
import org.techteam.decider.rest.api.GetCommentsRequest;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.api.GetUserRequest;
import org.techteam.decider.rest.api.LoginRegisterRequest;
import org.techteam.decider.rest.api.PollVoteRequest;
import org.techteam.decider.rest.api.UploadImageRequest;

import java.util.Collection;

public final class ServiceIntentBuilder {

    public static Intent getBasicIntent(Context context, String requestId, OperationType operationType) {
        Intent intent = new Intent(context, DeciderService.class);
        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
        intent.putExtra(DeciderService.IntentExtras.OPERATION, operationType.toString());
        return intent;
    }

    public static Intent getQuestionsIntent(Context context, OperationType op, String requestId, ContentSection contentSection, int limit, int offset, Collection<CategoryEntry> categories, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, op);

        intent.putExtra(GetQuestionsRequest.IntentExtras.CONTENT_SECTION, contentSection.toInt());
        intent.putExtra(GetQuestionsRequest.IntentExtras.LIMIT, limit);
        intent.putExtra(GetQuestionsRequest.IntentExtras.OFFSET, offset);

        int[] categories_ids = new int[categories.size()];
        int i = 0;
        for (CategoryEntry c : categories) {
            categories_ids[i++] = c.getUid();
        }
        intent.putExtra(GetQuestionsRequest.IntentExtras.CATEGORIES, categories_ids);
        intent.putExtra(GetQuestionsRequest.IntentExtras.LOAD_INTENTION, loadIntention);
        return intent;
    }

    public static Intent getCategoriesIntent(Context context, OperationType op, String requestId, String locale) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(GetCategoriesRequest.IntentExtras.LOCALE, locale);
        return intent;
    }

    public static Intent loginRegisterIntent(Context context, OperationType op, String requestId, String email, String password) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(LoginRegisterRequest.IntentExtras.EMAIL, email);
        intent.putExtra(LoginRegisterRequest.IntentExtras.PASSWORD, password);
        return intent;
    }

    public static Intent createQuestionIntent(Context context, OperationType op, String requestId, QuestionData questionData) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(CreateQuestionRequest.IntentExtras.QUESTION_DATA, questionData);
        return intent;
    }


    public static Intent uploadImageIntent(Context context, OperationType op, String requestId, ImageData image, int imageOrdinalId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(UploadImageRequest.IntentExtras.ORIGINAL_IMAGE, image.getOriginalFilename());
        intent.putExtra(UploadImageRequest.IntentExtras.PREVIEW_IMAGE, image.getPreviewFilename());
        intent.putExtra(UploadImageRequest.IntentExtras.IMAGE_ORDINAL_ID, imageOrdinalId);
        return intent;
    }

    public static Intent pollVoteIntent(Context context, OperationType op, String requestId, int questionId, int pollItemId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(PollVoteRequest.IntentExtras.QUESTION_ID, questionId);
        intent.putExtra(PollVoteRequest.IntentExtras.POLL_ITEM_ID, pollItemId);
        return intent;
    }

    public static Intent getCommentsIntent(Context context, OperationType op, String requestId, int questionId, int limit, int offset, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, op);

        intent.putExtra(GetCommentsRequest.IntentExtras.QUESTION_ID, questionId);
        intent.putExtra(GetCommentsRequest.IntentExtras.LIMIT, limit);
        intent.putExtra(GetCommentsRequest.IntentExtras.OFFSET, offset);
        intent.putExtra(GetCommentsRequest.IntentExtras.LOAD_INTENTION, loadIntention);
        return intent;
    }

    public static Intent createCommentIntent(Context context, OperationType op, String requestId, CommentData commentData) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(CreateCommentRequest.IntentExtras.COMMENT_DATA, commentData);
        return intent;
    }

    public static Intent getUserIntent(Context context, OperationType op, String requestId, String userId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(GetUserRequest.IntentExtras.USER_ID, userId);
        return intent;
    }
}
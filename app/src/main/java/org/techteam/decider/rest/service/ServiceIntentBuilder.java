package org.techteam.decider.rest.service;

import android.content.Context;
import android.content.Intent;

import org.techteam.decider.content.UserData;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CommentCreateRequest;
import org.techteam.decider.rest.api.EntityVoteRequest;
import org.techteam.decider.rest.api.QuestionCreateRequest;
import org.techteam.decider.rest.api.CategoriesGetRequest;
import org.techteam.decider.rest.api.CommentsGetRequest;
import org.techteam.decider.rest.api.QuestionsGetRequest;
import org.techteam.decider.rest.api.ReportSpamRequest;
import org.techteam.decider.rest.api.UserEditRequest;
import org.techteam.decider.rest.api.UserGetRequest;
import org.techteam.decider.rest.api.LoginRegisterRequest;
import org.techteam.decider.rest.api.PollVoteRequest;
import org.techteam.decider.rest.api.ImageUploadRequest;

import java.util.Collection;

public final class ServiceIntentBuilder {

    public static Intent getBasicIntent(Context context, String requestId, OperationType operationType) {
        Intent intent = new Intent(context, DeciderService.class);
        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
        intent.putExtra(DeciderService.IntentExtras.OPERATION, operationType.toString());
        return intent;
    }

    public static Intent getQuestionsIntent(Context context, OperationType op, String requestId, ContentSection contentSection, int firstQuestionId, int limit, int offset, Collection<CategoryEntry> categories, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, op);

        intent.putExtra(QuestionsGetRequest.IntentExtras.CONTENT_SECTION, contentSection.toInt());
        intent.putExtra(QuestionsGetRequest.IntentExtras.FIRST_QUESTION_ID, firstQuestionId);
        intent.putExtra(QuestionsGetRequest.IntentExtras.LIMIT, limit);
        intent.putExtra(QuestionsGetRequest.IntentExtras.OFFSET, offset);

        int[] categories_ids = new int[categories.size()];
        int i = 0;
        for (CategoryEntry c : categories) {
            categories_ids[i++] = c.getUid();
        }
        intent.putExtra(QuestionsGetRequest.IntentExtras.CATEGORIES, categories_ids);
        intent.putExtra(QuestionsGetRequest.IntentExtras.LOAD_INTENTION, loadIntention);
        return intent;
    }

    public static Intent getCategoriesIntent(Context context, OperationType op, String requestId, String locale) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(CategoriesGetRequest.IntentExtras.LOCALE, locale);
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
        intent.putExtra(QuestionCreateRequest.IntentExtras.QUESTION_DATA, questionData);
        return intent;
    }


    public static Intent uploadImageIntent(Context context, OperationType op, String requestId, ImageData image, int imageOrdinalId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(ImageUploadRequest.IntentExtras.ORIGINAL_IMAGE, image.getOriginalFilename());
        intent.putExtra(ImageUploadRequest.IntentExtras.PREVIEW_IMAGE, image.getPreviewFilename());
        intent.putExtra(ImageUploadRequest.IntentExtras.IMAGE_ORDINAL_ID, imageOrdinalId);
        return intent;
    }

    public static Intent pollVoteIntent(Context context, OperationType op, String requestId, int entryPosition, int questionId, int pollItemId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(PollVoteRequest.IntentExtras.ENTRY_POSITION, entryPosition);
        intent.putExtra(PollVoteRequest.IntentExtras.QUESTION_ID, questionId);
        intent.putExtra(PollVoteRequest.IntentExtras.POLL_ITEM_ID, pollItemId);
        return intent;
    }

    public static Intent likeQuestionIntent(Context context, OperationType op, String requestId, int entryPosition, int questionId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(EntityVoteRequest.IntentExtras.ENTRY_POSITION, entryPosition);
        intent.putExtra(EntityVoteRequest.IntentExtras.ENTITY_ID, questionId);
        return intent;
    }

    public static Intent getCommentsIntent(Context context, OperationType op, String requestId, int questionId, int limit, int offset, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, op);

        intent.putExtra(CommentsGetRequest.IntentExtras.QUESTION_ID, questionId);
        intent.putExtra(CommentsGetRequest.IntentExtras.LIMIT, limit);
        intent.putExtra(CommentsGetRequest.IntentExtras.OFFSET, offset);
        intent.putExtra(CommentsGetRequest.IntentExtras.LOAD_INTENTION, loadIntention);
        return intent;
    }

    public static Intent createCommentIntent(Context context, OperationType op, String requestId, CommentData commentData) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(CommentCreateRequest.IntentExtras.COMMENT_DATA, commentData);
        return intent;
    }

    public static Intent getUserIntent(Context context, OperationType op, String requestId, String userId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(UserGetRequest.IntentExtras.USER_ID, userId);
        return intent;
    }

    public static Intent getUserIntent(Context context, OperationType op, String requestId, String userId, String accessToken) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(UserGetRequest.IntentExtras.USER_ID, userId);
        intent.putExtra(UserGetRequest.IntentExtras.ACCESS_TOKEN, accessToken);
        return intent;
    }

    public static Intent editUserIntent(Context context, OperationType op, String requestId, UserData userData) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(UserEditRequest.IntentExtras.USER_DATA, userData);
        return intent;
    }

    public static Intent editUserIntent(Context context, OperationType op, String requestId, UserData userData, String accessToken) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(UserEditRequest.IntentExtras.USER_DATA, userData);
        intent.putExtra(UserEditRequest.IntentExtras.ACCESS_TOKEN, accessToken);
        return intent;
    }

    public static Intent reportSpamQuestionIntent(Context context, OperationType op, String requestId, int entryPosition, int questionId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(ReportSpamRequest.IntentExtras.ENTRY_POSITION, entryPosition);
        intent.putExtra(ReportSpamRequest.IntentExtras.ENTITY_ID, questionId);
        return intent;
    }

    public static Intent reportSpamCommentIntent(Context context, OperationType op, String requestId, int entryPosition, int commentId) {
        Intent intent = getBasicIntent(context, requestId, op);
        intent.putExtra(ReportSpamRequest.IntentExtras.ENTRY_POSITION, entryPosition);
        intent.putExtra(ReportSpamRequest.IntentExtras.ENTITY_ID, commentId);
        return intent;
    }
}
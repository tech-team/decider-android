package org.techteam.decider.rest.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.question.QuestionData;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.CreateQuestionRequest;
import org.techteam.decider.rest.api.GetCategoriesRequest;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.api.RegisterRequest;
import org.techteam.decider.rest.api.UploadImageRequest;

import java.util.Collection;

public final class ServiceIntentBuilder {

    public static Intent getBasicIntent(Context context, String requestId, OperationType operationType) {
        Intent intent = new Intent(context, DeciderService.class);
        intent.putExtra(DeciderService.IntentExtras.REQUEST_ID, requestId);
        intent.putExtra(DeciderService.IntentExtras.OPERATION, operationType.toString());
        return intent;
    }

    public static Intent getQuestionsIntent(Context context, String requestId, ContentSection contentSection, int limit, int offset, Collection<CategoryEntry> categories, int loadIntention) {
        Intent intent = getBasicIntent(context, requestId, OperationType.GET_QUESTIONS);

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

    public static Intent getCategoriesIntent(Context context, String requestId, String locale) {
        Intent intent = getBasicIntent(context, requestId, OperationType.GET_CATEGORIES);
        intent.putExtra(GetCategoriesRequest.IntentExtras.LOCALE, locale);
        return intent;
    }

    public static Intent registerIntent(Context context, String requestId, String email, String password) {
        Intent intent = getBasicIntent(context, requestId, OperationType.LOGIN_REGISTER);
        intent.putExtra(RegisterRequest.IntentExtras.EMAIL, email);
        intent.putExtra(RegisterRequest.IntentExtras.PASSWORD, password);
        return intent;
    }

    public static Intent createQuestionIntent(Context context, String requestId, QuestionData questionData) {
        Intent intent = getBasicIntent(context, requestId, OperationType.CREATE_QUESTION);
        intent.putExtra(CreateQuestionRequest.IntentExtras.QUESTION_DATA_JSON, questionData.toJson());
        return intent;
    }


    public static Intent uploadImageIntent(Context context, String requestId, Bitmap image) {
        Intent intent = getBasicIntent(context, requestId, OperationType.UPLOAD_IMAGE);
        intent.putExtra(UploadImageRequest.IntentExtras.IMAGE, image);
        return intent;
    }
}
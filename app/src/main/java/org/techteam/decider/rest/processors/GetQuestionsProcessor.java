package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import com.activeandroid.ActiveAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.db.resolvers.AbstractContentResolver;
import org.techteam.decider.db.resolvers.ContentResolver;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.api.InvalidAccessTokenException;
import org.techteam.decider.rest.api.TokenRefreshFailException;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.io.IOException;

public class GetQuestionsProcessor extends Processor {
    private final GetQuestionsRequest request;

    public GetQuestionsProcessor(Context context, GetQuestionsRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

//        transactionStarted(operationType, requestId);

        Bundle result = getInitialBundle();
        try {
            JSONObject response = apiUI.getQuestions(request);
            System.out.println(response);

//            ContentResolver resolver = AbstractContentResolver.getResolver(request.getContentSection());


            if (request.getLoadIntention() == LoadIntention.REFRESH) {
//                    resolver.deleteAllEntries(getContext());
            }

            if (!response.getString("status").equalsIgnoreCase("ok")) {
                System.err.println("not ok!");
                return;
            }

            ActiveAndroid.beginTransaction();
            try {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); ++i) {
                    JSONObject q = data.getJSONObject(i);
                    QuestionEntry entry = QuestionEntry.fromJson(q);
                    entry.saveTotal();
//                    resolver.insert(getContext(), QuestionEntry.fromJson(q));
                }
                ActiveAndroid.setTransactionSuccessful();

                result.putInt(ServiceCallback.GetQuestionsExtras.COUNT, data.length());
            } finally {
                ActiveAndroid.endTransaction();
            }

            cb.onSuccess(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
        } catch (TokenRefreshFailException e) {
            e.printStackTrace();
        }


//        ContentList list = null;
//        Throwable exc = null;
//        try {
//            list = contentSource.retrieveNextList(getContext());
//        } catch (FeedOverException ignored) {
//
//        } catch (ContentParseException e) {
//            exc = e;
//        }
//
//        if (list == null) {
//            if (exc != null) {
//                transactionError(operationType, requestId);
//                cb.onError(exc.getMessage(), null);
//            } else {
//                transactionFinished(operationType, requestId);
//                Bundle data = getInitialBundle();
//                data.putBoolean(ServiceCallback.GetPostsExtras.FEED_FINISHED, true);
//                cb.onSuccess(data);
//            }
//
//            System.out.println("DONE! list is null");
//        } else {
//            System.out.println("DONE! + " + list.getEntries().size());
//
//            ContentResolver resolver = AbstractContentResolver.getResolver(contentSection);
//
//            int insertedCount = list.getEntries().size();
//            if (resolver != null) {
//                if (loadIntention == LoadIntention.REFRESH) {
//                    resolver.deleteAllEntries(getContext());
//                }
//
//                // writing to db
//                insertedCount = resolver.insertEntries(getContext(), list).size();
//            }

            // finishing up a transaction
//            transactionFinished(operationType, requestId);

//
//            data.putInt(ServiceCallback.GetPostsExtras.INSERTED_COUNT, insertedCount);
//            cb.onSuccess(data);
//        }
    }

    private Bundle getInitialBundle() {
        Bundle data = new Bundle();
        data.putInt(ServiceCallback.GetQuestionsExtras.LOAD_INTENTION, request.getLoadIntention());
        return data;
    }
}

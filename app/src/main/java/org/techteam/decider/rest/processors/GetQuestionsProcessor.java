package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.db.resolvers.AbstractContentResolver;
import org.techteam.decider.db.resolvers.ContentResolver;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.net.HttpDownloader;
import org.techteam.decider.net.UrlParams;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.GetQuestionsRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.content.ContentSection;

import java.io.IOException;

public class GetQuestionsProcessor extends Processor {
    private final GetQuestionsRequest request;

    private static final String URL_PATH = "/questions";

    public GetQuestionsProcessor(Context context, GetQuestionsRequest request) {
        super(context);
        this.request = request;
    }

    @Override
    public void start(OperationType operationType, String requestId, ProcessorCallback cb) {

        transactionStarted(operationType, requestId);

        try {
            JSONObject response = apiUI.getQuestionsRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        try {
//            UrlParams params = new UrlParams();
//            params.add("limit", limit);
//            params.add("offset", offset);
//            HttpDownloader.httpGet(URL_PATH);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


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
            transactionFinished(operationType, requestId);

            Bundle data = getInitialBundle();
//            data.putInt(ServiceCallback.GetPostsExtras.INSERTED_COUNT, insertedCount);
            cb.onSuccess(data);
//        }
    }

    private Bundle getInitialBundle() {
        Bundle data = new Bundle();
//        data.putParcelable(ServiceCallback.GetPostsExtras.NEW_CONTENT_SOURCE, contentSource);
//        data.putInt(ServiceCallback.GetPostsExtras.LOAD_INTENTION, loadIntention);
        return data;
    }
}

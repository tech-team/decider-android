package org.techteam.decider.rest.processors;

import android.os.Bundle;

public interface ProcessorCallback {
    void onSuccess(Bundle data);
    void onError(String message, Bundle data);
}

package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.UserData;

public class PushAuthRequest {
    public static final String URL = "auth";

    private final String instanceId;
    private final String regToken;

    public class IntentExtras {
        public static final String INSTANCE_ID = "INSTANCE_ID";
        public static final String REG_TOKEN = "REG_TOKEN";
    }

    public PushAuthRequest(String instanceId, String regToken) {
        this.instanceId = instanceId;
        this.regToken = regToken;
    }

    public static PushAuthRequest fromBundle(Bundle bundle) {
        String instanceId = bundle.getString(IntentExtras.INSTANCE_ID);
        String regToken = bundle.getString(IntentExtras.REG_TOKEN);
        return new PushAuthRequest(instanceId, regToken);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getRegToken() {
        return regToken;
    }
}

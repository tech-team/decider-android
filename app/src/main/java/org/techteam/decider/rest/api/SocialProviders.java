package org.techteam.decider.rest.api;

public class SocialProviders {
    public static final String SOCIAL_PATH = "social_login";
    public static final String SOCIAL_COMPLETE = "social_complete";

    public enum Provider {
        VK("vk");

        private String providerPath;

        Provider(String providerPath) {
            this.providerPath = providerPath;
        }

        public String getProviderPath() {
            return providerPath;
        }

    }
    public static String getProviderPath(Provider provider) {
        return ApiUI.resolveApiUrl(SOCIAL_PATH, provider.getProviderPath());
    }

    public static String getSocialCompletePath() {
        return ApiUI.resolveApiUrl(SOCIAL_COMPLETE);
    }
}
